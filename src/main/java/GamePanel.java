import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {
    public static final int WIDTH = 400;
    public static final int HEIGHT = 600;

    private final Player player;
    private final List<Bullet> bullets;
    private BufferedImage backBuffer;
    private volatile boolean running;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        player = new Player((WIDTH - 40) / 2.0, HEIGHT - 80, 40, 40, 100, WIDTH, HEIGHT);
        bullets = new ArrayList<>();
        addKeyListener(new KeyInput(player));

        startGameLoop();
    }

    public Player getPlayer() {
        return player;
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
        final long targetFrameNanos = 1_000_000_000L / 60;

        while (running) {
            long currentTime = System.nanoTime();
            double delta = (currentTime - lastTime) / 1_000_000_000.0;
            lastTime = currentTime;

            player.update(delta);
            player.getWeapon().update(delta);
            if (player.getWeapon().canShoot()) {
                bullets.addAll(player.shoot());
            }

            for (Bullet bullet : bullets) {
                bullet.update(delta);
            }
            bullets.removeIf(bullet -> !bullet.isActive());

            repaint();

            long elapsed = System.nanoTime() - currentTime;
            long sleepNanos = targetFrameNanos - elapsed;
            if (sleepNanos > 0) {
                long sleepMillis = sleepNanos / 1_000_000L;
                int sleepNanosPart = (int) (sleepNanos % 1_000_000L);
                try {
                    Thread.sleep(sleepMillis, sleepNanosPart);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    running = false;
                }
            }
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

        player.draw(bufferGraphics);
        for (Bullet bullet : bullets) {
            bullet.draw(bufferGraphics);
        }

        bufferGraphics.dispose();
        g.drawImage(backBuffer, 0, 0, null);
    }
}
