import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class EnemyCollisionTest {

    @Test
    void enemyUpdateUsesDeltaForDescendingMovement() {
        SimpleEnemy enemy = new SimpleEnemy(100, 0, 100);

        enemy.update(0.5);

        assertEquals(50.0, enemy.getY(), 1e-9);
    }

    @Test
    void overlappingPlayerBulletAndEnemyDeactivateBothAndIncreaseScore() {
        GamePanel panel = new GamePanel(false);
        Bullet bullet = new Bullet(100, 100, 30, 30, 0, 0, true, 1);
        SimpleEnemy enemy = new SimpleEnemy(100, 100, 30, 30, 100, 1, 10);

        panel.addBullet(bullet);
        panel.addEnemy(enemy);

        panel.checkCollisions();

        assertFalse(bullet.isActive());
        assertFalse(enemy.isActive());
        assertEquals(10, panel.getScore());
    }

    @Test
    void enemyDeactivatesAfterEscapingBottomBoundary() {
        SimpleEnemy enemy = new SimpleEnemy(100, 605, 20);

        enemy.update(0.5);

        assertFalse(enemy.isActive());
    }

    @Test
    void playerTakesDamageWhenCollidingWithEnemy() {
        GamePanel panel = new GamePanel(false);
        Player player = panel.getPlayer();
        int initialHp = player.getHp();
        SimpleEnemy enemy = new SimpleEnemy(player.getX(), player.getY(), player.getWidth(), player.getHeight(), 100, 1, 10);

        panel.addEnemy(enemy);
        panel.checkCollisions();

        assertFalse(enemy.isActive());
        assertEquals(initialHp - 10, player.getHp());
    }
}
