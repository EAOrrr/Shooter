import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Canvas;
import java.awt.event.KeyEvent;
import org.junit.jupiter.api.Test;

class DebugModeTest {

    @Test
    void godModePreventsPlayerDamage() {
        Player player = new Player(100, 200, 40, 40, 100, 400, 600);
        player.setInvincible(true);

        player.takeDamage(50);

        assertEquals(100, player.getHp());
    }

    @Test
    void debugHotkeysToggleFlagsAndCanSpawnBoss() {
        GamePanel panel = new GamePanel(false);
        Player player = panel.getPlayer();
        KeyInput keyInput = new KeyInput(player, panel);

        panel.getEnemies().clear();
        assertFalse(player.isInvincible());
        assertFalse(panel.isShowHitbox());

        keyInput.keyPressed(new KeyEvent(new Canvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_F1, KeyEvent.CHAR_UNDEFINED));
        keyInput.keyPressed(new KeyEvent(new Canvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_F3, KeyEvent.CHAR_UNDEFINED));
        keyInput.keyPressed(new KeyEvent(new Canvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_F4, KeyEvent.CHAR_UNDEFINED));

        assertTrue(player.isInvincible());
        assertTrue(panel.isShowHitbox());
        assertTrue(panel.getEnemies().stream().anyMatch(enemy -> enemy instanceof BossEnemy));
        assertEquals(1, panel.getEnemies().size());
    }
}
