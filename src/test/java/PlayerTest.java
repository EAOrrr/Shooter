import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlayerTest {

    @Test
    void updateUsesDeltaForVelocityScaling() {
        Player player = new Player(100, 200, 40, 40, 100, 400, 600);
        player.setSpeed(200, 0);

        player.update(0.5);

        assertEquals(200.0, player.getX(), 1e-9);
        assertEquals(200.0, player.getY(), 1e-9);
    }

    @Test
    void takeDamageReducesHpAndKeepsPlayerActiveWhenHpRemains() {
        Player player = new Player(100, 200, 40, 40, 100, 400, 600);

        player.takeDamage(30);

        assertEquals(70, player.getHp());
        assertTrue(player.isActive());
    }

    @Test
    void fatalDamageSetsHpToZeroAndDeactivatesPlayer() {
        Player player = new Player(100, 200, 40, 40, 100, 400, 600);

        player.takeDamage(100);

        assertEquals(0, player.getHp());
        assertFalse(player.isActive());
    }

    @Test
    void updateClampsPositionToWindowBounds() {
        Player player = new Player(10, 200, 40, 40, 100, 400, 600);
        player.setSpeed(-500, 0);

        player.update(1.0);

        assertEquals(0.0, player.getX(), 1e-9);
    }
}