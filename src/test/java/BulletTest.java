import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class BulletTest {

    @Test
    void updateAppliesDeltaToVelocity() {
        Bullet bullet = new Bullet(100, 500, 0, -400, true, 1);

        bullet.update(0.5);

        assertEquals(300.0, bullet.getY(), 1e-9);
    }

    @Test
    void updateDeactivatesWhenOffScreen() {
        Bullet bullet = new Bullet(100, -5, 0, -100, true, 1);

        bullet.update(0.016);

        assertFalse(bullet.isActive());
    }
}
