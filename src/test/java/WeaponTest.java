import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class WeaponTest {

    @Test
    void simpleShotWeaponReturnsParallelBulletsAtExpectedXPositions() {
        SimpleShotWeapon weapon = new SimpleShotWeapon(3, 20, 0.2);
        weapon.update(0.2);

        List<Bullet> bullets = weapon.shoot(100, 500, true);

        assertEquals(3, bullets.size());
        assertEquals(90.0, bullets.get(0).getX(), 1e-9);
        assertEquals(100.0, bullets.get(1).getX(), 1e-9);
        assertEquals(110.0, bullets.get(2).getX(), 1e-9);
    }

    @Test
    void cooldownAccumulatorTriggersAfterEnoughDeltaAndResetsOnShoot() {
        SimpleShotWeapon weapon = new SimpleShotWeapon(1, 0, 0.2);

        weapon.update(0.1);
        assertTrue(weapon.shoot(100, 500, true).isEmpty());
        assertEquals(0.1, weapon.getCooldownTimer(), 1e-9);

        weapon.update(0.15);
        assertEquals(1, weapon.shoot(100, 500, true).size());
        assertEquals(0.0, weapon.getCooldownTimer(), 1e-9);
    }
}
