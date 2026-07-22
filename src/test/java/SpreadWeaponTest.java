import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import org.junit.jupiter.api.Test;

class SpreadWeaponTest {

    @Test
    void symmetricVelocityVectorsForThreeShotThirtyDegreeSpread() {
        SpreadWeapon weapon = new SpreadWeapon(3, 30, 500, 0.2);
        weapon.update(0.2);

        List<Bullet> bullets = weapon.shoot(100, 500, true);

        assertEquals(3, bullets.size());
        bullets.forEach(bullet -> bullet.update(1.0));

        assertEquals(-500.0 * Math.sin(Math.toRadians(15)), bullets.get(0).getX() - 100.0, 1e-6);
        assertEquals(0.0, bullets.get(1).getX() - 100.0, 1e-9);
        assertEquals(500.0 * Math.sin(Math.toRadians(15)), bullets.get(2).getX() - 100.0, 1e-6);
        assertEquals(500.0 - 500.0 * Math.cos(Math.toRadians(15)), bullets.get(0).getY(), 1e-6);
        assertEquals(0.0, bullets.get(1).getY(), 1e-9);
        assertEquals(500.0 - 500.0 * Math.cos(Math.toRadians(15)), bullets.get(2).getY(), 1e-6);
    }

    @Test
    void bulletUpdateMovesAlongAngleByDelta() {
        Bullet bullet = new Bullet(100, 200, 100, -200, true, 1);

        bullet.update(0.5);

        assertEquals(150.0, bullet.getX(), 1e-9);
        assertEquals(100.0, bullet.getY(), 1e-9);
    }

    @Test
    void upgradeWeaponReplacesSimpleShotWithSpreadWeapon() {
        Player player = new Player(100, 200, 40, 40, 100, 400, 600, new SimpleShotWeapon(1, 0, 0.2));

        player.upgradeWeapon();

        assertInstanceOf(SpreadWeapon.class, player.getWeapon());
    }
}