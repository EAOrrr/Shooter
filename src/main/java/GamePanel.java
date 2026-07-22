import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {
    enum GameState {
        RUNNING,
        PENDING,
        END
    }

    public static final int WIDTH = 400;
    public static final int HEIGHT = 600;
    private static final int INITIAL_BOSS_SCORE_THRESHOLD = 200;
    private static final int BOSS_SCORE_INCREMENT = 300;
    private static final double ENEMY_SPAWN_INTERVAL = 1.0;
    private static final long TARGET_FRAME_NANOS = 1_000_000_000L / 60;
    private static final double MAX_DELTA_SECONDS = 0.05;
    private static final int PLAYER_COLLISION_DAMAGE = 10;
    private static final double FAST_ENEMY_SPAWN_PROBABILITY = 0.50;
    private static final double STANDARD_ENEMY_SPAWN_PROBABILITY = 0.35;

    private final Player player;
    private final List<Bullet> bullets;
    private final List<Enemy> enemies;
    private final Random random;
    private final Object stateLock;
    private int score;
    private int bestScore;
    private int nextBossScoreThreshold;
    private double spawnTimer;
    private BufferedImage backBuffer;
    private volatile boolean running;
    private volatile boolean showHitbox;
    private volatile GameState gameState;

    public GamePanel() {
        this(true);
    }

    public GamePanel(boolean autoStart) {
        this(autoStart, new Random());
    }

    GamePanel(boolean autoStart, Random random) {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);

        this.random = random;
        player = new Player((WIDTH - 40) / 2.0, HEIGHT - 80, 40, 40, 1000, WIDTH, HEIGHT);
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        stateLock = new Object();
        gameState = GameState.RUNNING;
        nextBossScoreThreshold = INITIAL_BOSS_SCORE_THRESHOLD;
        addKeyListener(new KeyInput(player, this));

        if (autoStart) {
            startGameLoop();
        }
    }

    public Player getPlayer() {
        return player;
    }

    int getScore() {
        return score;
    }

    int getBestScore() {
        return bestScore;
    }

    List<Bullet> getBullets() {
        return bullets;
    }

    List<Enemy> getEnemies() {
        return enemies;
    }

    int getNextBossScoreThreshold() {
        return nextBossScoreThreshold;
    }

    GameState getGameState() {
        return gameState;
    }

    boolean isShowHitbox() {
        return showHitbox;
    }

    void setShowHitbox(boolean showHitbox) {
        this.showHitbox = showHitbox;
    }

    void toggleShowHitbox() {
        showHitbox = !showHitbox;
    }

    void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }

    void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    void restartGame() {
        synchronized (stateLock) {
            score = 0;
            nextBossScoreThreshold = INITIAL_BOSS_SCORE_THRESHOLD;
            spawnTimer = 0.0;
            showHitbox = false;
            bullets.clear();
            enemies.clear();
            player.resetToSpawn();
            gameState = GameState.RUNNING;
        }
    }

    private void startGameLoop() {
        if (running) {
            return;
        }

        running = true;
        Thread gameThread = new Thread(this, "game-loop");
        gameThread.setDaemon(true);
        gameThread.start();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        long nextFrameAt = lastTime + TARGET_FRAME_NANOS;

        while (running) {
            long currentTime = System.nanoTime();
            double delta = Math.min(MAX_DELTA_SECONDS, (currentTime - lastTime) / 1_000_000_000.0);
            lastTime = currentTime;

            synchronized (stateLock) {
                if (gameState == GameState.RUNNING) {
                    player.update(delta);
                    player.getWeapon().update(delta);
                    if (player.getWeapon().canShoot()) {
                        bullets.addAll(player.shoot());
                    }

                    spawnEnemies(delta);

                    for (Bullet bullet : bullets) {
                        bullet.update(delta);
                    }
                    for (Enemy enemy : enemies) {
                        enemy.update(delta);
                        enemy.getWeapon().update(delta);
                        if (enemy.getWeapon().canShoot()) {
                            bullets.addAll(enemy.shoot());
                        }
                    }

                    checkCollisions();
                    updateGameState();
                }

                bullets.removeIf(bullet -> !bullet.isActive());
                enemies.removeIf(enemy -> !enemy.isActive());
            }

            repaint();

            long sleepNanos = nextFrameAt - System.nanoTime();
            if (sleepNanos > 0) {
                LockSupport.parkNanos(sleepNanos);
            } else if (sleepNanos < -TARGET_FRAME_NANOS * 2) {
                // If the loop falls far behind, resync to avoid long-term timing drift.
                nextFrameAt = System.nanoTime();
            }

            nextFrameAt += TARGET_FRAME_NANOS;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        if (panelWidth <= 0 || panelHeight <= 0) {
            return;
        }

        if (backBuffer == null || backBuffer.getWidth() != panelWidth || backBuffer.getHeight() != panelHeight) {
            backBuffer = new BufferedImage(panelWidth, panelHeight, BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D bufferGraphics = backBuffer.createGraphics();
        bufferGraphics.setColor(getBackground());
        bufferGraphics.fillRect(0, 0, panelWidth, panelHeight);

        synchronized (stateLock) {
            if (player.isActive()) {
                player.draw(bufferGraphics);
            }
            for (Enemy enemy : enemies) {
                enemy.draw(bufferGraphics);
            }
            for (Bullet bullet : bullets) {
                bullet.draw(bufferGraphics);
            }

            drawHud(bufferGraphics);
            drawGameStateOverlay(bufferGraphics);

            if (showHitbox) {
                drawHitboxes(bufferGraphics);
            }
        }

        bufferGraphics.dispose();
        g.drawImage(backBuffer, 0, 0, null);
    }

    void spawnEnemies(double delta) {
        if (findActiveBoss() != null) {
            return;
        }

        if (score >= nextBossScoreThreshold) {
            spawnBossIfEligible();
            return;
        }

        if (delta <= 0) {
            return;
        }

        spawnTimer += delta;
        while (spawnTimer >= ENEMY_SPAWN_INTERVAL) {
            spawnTimer -= ENEMY_SPAWN_INTERVAL;
            enemies.add(createRandomEnemy());
        }
    }

    void spawnBoss() {
        synchronized (stateLock) {
            spawnBoss(true);
        }
    }

    private void spawnBossIfEligible() {
        spawnBoss(false);
    }

    private void spawnBoss(boolean force) {
        if (!force && score < nextBossScoreThreshold) {
            return;
        }

        boolean hasActiveBoss = enemies.stream().anyMatch(enemy -> enemy instanceof BossEnemy && enemy.isActive());
        if (hasActiveBoss) {
            return;
        }

        enemies.removeIf(enemy -> !(enemy instanceof BossEnemy));

        double bossX = (WIDTH - BossEnemy.DEFAULT_WIDTH) / 2.0;
        enemies.add(new BossEnemy(bossX, 40));
    }

    private Enemy createRandomEnemy() {
        double roll = random.nextDouble();
        if (roll < FAST_ENEMY_SPAWN_PROBABILITY) {
            return createFastEnemy();
        }

        if (roll < FAST_ENEMY_SPAWN_PROBABILITY + STANDARD_ENEMY_SPAWN_PROBABILITY) {
            return createStandardEnemy();
        }

        return createHeavyEnemy();
    }

    private Enemy createFastEnemy() {
        int size = randomInclusive(20, 28);
        int hp = randomInclusive(1, 2);
        double speedY = randomRange(150.0, 240.0);
        Weapon weapon = createRandomEnemyWeapon();
        double x = randomRange(0.0, WIDTH - size);
        return new FastEnemy(x, -size, size, size, speedY, hp, weapon, 15);
    }

    private Enemy createStandardEnemy() {
        int size = randomInclusive(28, 38);
        int hp = randomInclusive(2, 4);
        double speedY = randomRange(90.0, 160.0);
        Weapon weapon = createRandomEnemyWeapon();
        double x = randomRange(0.0, WIDTH - size);
        return new SimpleEnemy(x, -size, size, size, speedY, hp, 30, weapon);
    }

    private Enemy createHeavyEnemy() {
        int size = randomInclusive(40, 56);
        int hp = randomInclusive(5, 8);
        double speedY = randomRange(55.0, 110.0);
        Weapon weapon = createRandomEnemyWeapon();
        double x = randomRange(0.0, WIDTH - size);
        return new HeavyEnemy(x, -size, size, size, speedY, hp, weapon, 60);
    }

    private Weapon createRandomEnemyWeapon() {
        int streamCount = randomInclusive(1, 3);
        double spreadWidth = streamCount == 1 ? 0.0 : randomRange(12.0, 40.0);
        double cooldownInterval = randomRange(0.55, 1.50);
        return new SimpleShotWeapon(new WeaponStats(streamCount, spreadWidth, cooldownInterval, 1));
    }

    private int randomInclusive(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private double randomRange(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    Enemy peekSpawnedEnemyForTesting() {
        return createRandomEnemy();
    }

    void checkCollisions() {
        for (Bullet bullet : bullets) {
            if (!bullet.isActive()) {
                continue;
            }

            if (!bullet.isFromPlayer()) {
                if (player.isActive() && bullet.intersects(player)) {
                    bullet.setActive(false);
                    player.takeDamage(bullet.getDamage());
                }
                continue;
            }

            for (Enemy enemy : enemies) {
                if (!enemy.isActive()) {
                    continue;
                }

                if (bullet.intersects(enemy)) {
                    bullet.setActive(false);
                    enemy.takeDamage(bullet.getDamage());

                    if (!enemy.isActive()) {
                        score += enemy.getScoreValue();
                        bestScore = Math.max(bestScore, score);
                        if (enemy instanceof BossEnemy) {
                            nextBossScoreThreshold = score + BOSS_SCORE_INCREMENT;
                        }
                    }
                    break;
                }
            }
        }

        spawnBossIfEligible();

        if (!player.isActive()) {
            updateGameState();
            return;
        }

        for (Enemy enemy : enemies) {
            if (!enemy.isActive()) {
                continue;
            }

            if (player.intersects(enemy)) {
                player.takeDamage(PLAYER_COLLISION_DAMAGE);
                if (!(enemy instanceof BossEnemy)) {
                    enemy.setActive(false);
                }
            }
        }

        updateGameState();
    }

    private void drawHitboxes(Graphics2D g) {
        g.setColor(Color.GREEN);
        g.drawRect((int) Math.round(player.getX()), (int) Math.round(player.getY()), player.getWidth(), player.getHeight());

        for (Enemy enemy : enemies) {
            g.drawRect((int) Math.round(enemy.getX()), (int) Math.round(enemy.getY()), enemy.getWidth(), enemy.getHeight());
        }

        for (Bullet bullet : bullets) {
            g.drawRect((int) Math.round(bullet.getX()), (int) Math.round(bullet.getY()), bullet.getWidth(), bullet.getHeight());
        }
    }

    private void drawHud(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.drawString("Player HP: " + player.getHp() + "/" + player.getMaxHp(), 10, 20);
        g.drawString("Score: " + score, 10, 36);
        g.drawString("Best: " + bestScore, 10, 52);

        BossEnemy boss = findActiveBoss();
        if (boss == null) {
            return;
        }

        int barX = 10;
        int barY = 62;
        int barWidth = WIDTH - 20;
        int barHeight = 12;
        double ratio = Math.max(0.0, Math.min(1.0, boss.getHp() / (double) boss.getMaxHp()));
        int filledWidth = (int) Math.round(barWidth * ratio);

        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barWidth, barHeight);
        g.setColor(Color.RED);
        g.fillRect(barX, barY, filledWidth, barHeight);
        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, barWidth, barHeight);
        g.drawString("Boss HP: " + boss.getHp() + "/" + boss.getMaxHp(), barX, barY + barHeight + 14);
    }

    private void drawGameStateOverlay(Graphics2D g) {
        if (gameState != GameState.END) {
            return;
        }

        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.WHITE);
        g.drawString("GAME OVER", WIDTH / 2 - 38, HEIGHT / 2 - 6);
        g.drawString("Press R to Restart", WIDTH / 2 - 58, HEIGHT / 2 + 18);
    }

    private BossEnemy findActiveBoss() {
        for (Enemy enemy : enemies) {
            if (enemy instanceof BossEnemy boss && boss.isActive()) {
                return boss;
            }
        }
        return null;
    }

    private void updateGameState() {
        if (!player.isActive()) {
            gameState = GameState.END;
        }
    }
}
