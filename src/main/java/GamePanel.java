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
        addKeyListener(new KeyInput(player));

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

    void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }

    void addEnemy(Enemy enemy) {
        enemies.add(enemy);
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
                }

                checkCollisions();

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
            player.draw(bufferGraphics);
            for (Enemy enemy : enemies) {
                enemy.draw(bufferGraphics);
            }
            for (Bullet bullet : bullets) {
                bullet.draw(bufferGraphics);
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
            enemies.add(new SimpleEnemy(x, -30, 100, 10));
        }
    }

    void checkCollisions() {
        for (Bullet bullet : bullets) {
            if (!bullet.isActive() || !bullet.isFromPlayer()) {
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
            return;
        }

        for (Enemy enemy : enemies) {
            if (!enemy.isActive()) {
                continue;
            }

            if (player.intersects(enemy)) {
                enemy.setActive(false);
                player.takeDamage(PLAYER_COLLISION_DAMAGE);
            }
        }
    }
}
