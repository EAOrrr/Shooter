import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Canvas;
import java.awt.event.KeyEvent;
import org.junit.jupiter.api.Test;

class GameStateTest {

    @Test
    void gameTransitionsToEndOnPlayerDeathAndCanRestartWithR() {
        GamePanel panel = new GamePanel(false);
        Player player = panel.getPlayer();

        Bullet fatalEnemyBullet = new Bullet(player.getX(), player.getY(), player.getWidth(), player.getHeight(), 0, 0, false, player.getHp());
        panel.addBullet(fatalEnemyBullet);
        panel.checkCollisions();

        assertEquals(GamePanel.GameState.END, panel.getGameState());

        KeyInput keyInput = new KeyInput(player, panel);
        keyInput.keyPressed(new KeyEvent(new Canvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_R, KeyEvent.CHAR_UNDEFINED));

        assertEquals(GamePanel.GameState.RUNNING, panel.getGameState());
        assertEquals(player.getMaxHp(), player.getHp());
        assertTrue(player.isActive());
        assertTrue(panel.getEnemies().stream().anyMatch(enemy -> enemy instanceof BossEnemy));
    }
}
