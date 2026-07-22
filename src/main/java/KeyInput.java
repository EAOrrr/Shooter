import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyInput extends KeyAdapter {
    public static final double SPEED_PX_PER_SEC = 300.0;

    private final Player player;
    private final GamePanel gamePanel;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean upPressed;
    private boolean downPressed;

    public KeyInput(Player player) {
        this(player, null);
    }

    public KeyInput(Player player, GamePanel gamePanel) {
        this.player = player;
        this.gamePanel = gamePanel;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> leftPressed = true;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> rightPressed = true;
            case KeyEvent.VK_W, KeyEvent.VK_UP -> upPressed = true;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> downPressed = true;
            case KeyEvent.VK_F1 -> player.setInvincible(!player.isInvincible());
            case KeyEvent.VK_F2 -> player.upgradeWeapon();
            case KeyEvent.VK_F3 -> {
                if (gamePanel != null) {
                    gamePanel.toggleShowHitbox();
                }
            }
            case KeyEvent.VK_F4 -> {
                if (gamePanel != null) {
                    gamePanel.spawnBoss();
                }
            }
            case KeyEvent.VK_R -> {
                if (gamePanel != null && gamePanel.getGameState() == GamePanel.GameState.END) {
                    gamePanel.restartGame();
                }
            }
            default -> {
                return;
            }
        }
        applyVelocity();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> leftPressed = false;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> rightPressed = false;
            case KeyEvent.VK_W, KeyEvent.VK_UP -> upPressed = false;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> downPressed = false;
            default -> {
                return;
            }
        }
        applyVelocity();
    }

    private void applyVelocity() {
        double velocityX = 0.0;
        double velocityY = 0.0;

        if (leftPressed ^ rightPressed) {
            velocityX = leftPressed ? -SPEED_PX_PER_SEC : SPEED_PX_PER_SEC;
        }

        if (upPressed ^ downPressed) {
            velocityY = upPressed ? -SPEED_PX_PER_SEC : SPEED_PX_PER_SEC;
        }

        player.setSpeed(velocityX, velocityY);
    }
}
