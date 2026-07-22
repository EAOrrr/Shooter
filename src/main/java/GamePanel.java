import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
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
    private static final double ENEMY_SPAWN_INTERVAL = 1.0;
    private static final long TARGET_FRAME_NANOS = 1_000_000_000L / 60;
    private static final double MAX_DELTA_SECONDS = 0.05;
    private static final int PLAYER_COLLISION_DAMAGE = 10;

    private final Player player;
    private final List<Bullet> bullets;
    private final List<Enemy> enemies;
    private final Object stateLock;
    private int score;
    private double spawnTimer;
    private BufferedImage backBuffer;
    private volatile boolean running;
    private volatile boolean showHitbox;
    private volatile GameState gameState;

    public GamePanel() {
        this(true);
    }

    public GamePanel(boolean autoStart) {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);

        player = new Player((WIDTH - 40) / 2.0, HEIGHT - 80, 40, 40, 20, WIDTH, HEIGHT);
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        stateLock = new Object();
        gameState = GameState.RUNNING;
        addKeyListener(new KeyInput(player, this));
        spawnBoss();

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

    List<Bullet> getBullets() {
        return bullets;
    }

    List<Enemy> getEnemies() {
        return enemies;
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
            spawnTimer = 0.0;
            showHitbox = false;
            bullets.clear();
            enemies.clear();
            player.resetToSpawn();
            gameState = GameState.RUNNING;
            spawnBoss();
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
        if (delta <= 0) {
            return;
        }

        spawnTimer += delta;
        while (spawnTimer >= ENEMY_SPAWN_INTERVAL) {
            spawnTimer -= ENEMY_SPAWN_INTERVAL;
            double x = Math.random() * (WIDTH - 30);
            enemies.add(new SimpleEnemy(x, -30, 100, 1));
        }
    }

    void spawnBoss() {
        synchronized (stateLock) {
            boolean hasActiveBoss = enemies.stream().anyMatch(enemy -> enemy instanceof BossEnemy && enemy.isActive());
            if (hasActiveBoss) {
                return;
            }

            double bossX = (WIDTH - BossEnemy.DEFAULT_WIDTH) / 2.0;
            enemies.add(new BossEnemy(bossX, 40));
        }
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
                    }
                    break;
                }
            }
        }

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

        BossEnemy boss = findActiveBoss();
        if (boss == null) {
            return;
        }

        int barX = 10;
        int barY = 30;
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
