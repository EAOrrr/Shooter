import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class BossTest {

    @Test
    void bossStartsWithSimpleShotWeaponAtFullHp() {
        BossEnemy boss = new BossEnemy(150, 40);

        assertEquals(200, boss.getHp());
        assertInstanceOf(SimpleShotWeapon.class, boss.getWeapon());
    }

    @Test
    void bossMutatesWeaponToSpreadWhenHpDropsBelowHalf() {
        BossEnemy boss = new BossEnemy(150, 40);

        boss.takeDamage(140);
        boss.update(0.016);

        assertEquals(60, boss.getHp());
        assertInstanceOf(SpreadWeapon.class, boss.getWeapon());
        assertEquals(2, boss.getPhase());
    }

    @Test
    void bossOscillatesHorizontallyUsingDeltaAccumulator() {
        BossEnemy boss = new BossEnemy(150, 40);
        double initialX = boss.getX();

        boss.update(0.5);

        assertNotEquals(initialX, boss.getX());
        assertEquals(0.5, boss.getTimeAccumulator(), 1e-9);
    }
}
