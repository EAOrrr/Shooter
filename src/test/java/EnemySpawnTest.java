import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

class EnemySpawnTest {

    @Test
    void spawnProfilesMapRepresentativeProbabilityBandsToDistinctEnemyTypes() {
        GamePanel fastPanel = new GamePanel(false, new StubRandom(new double[] {0.10, 0.20, 0.35, 0.40, 0.50}, new int[] {2, 0, 1}));
        GamePanel standardPanel = new GamePanel(false, new StubRandom(new double[] {0.70, 0.25, 0.60, 0.45, 0.55}, new int[] {4, 1, 2}));
        GamePanel heavyPanel = new GamePanel(false, new StubRandom(new double[] {0.95, 0.30, 0.50, 0.60, 0.75}, new int[] {6, 2, 1}));

        assertInstanceOf(FastEnemy.class, fastPanel.peekSpawnedEnemyForTesting());
        assertInstanceOf(SimpleEnemy.class, standardPanel.peekSpawnedEnemyForTesting());
        assertInstanceOf(HeavyEnemy.class, heavyPanel.peekSpawnedEnemyForTesting());
    }

    @Test
    void spawnedEnemyReceivesRandomizedWeaponHealthSizeAndScoreValues() {
        GamePanel panel = new GamePanel(false, new StubRandom(new double[] {0.70, 0.50, 0.25, 0.40, 0.80}, new int[] {5, 2, 2}));

        Enemy enemy = panel.peekSpawnedEnemyForTesting();

        assertInstanceOf(SimpleEnemy.class, enemy);
        assertEquals(33, enemy.getWidth());
        assertEquals(33, enemy.getHeight());
        assertEquals(4, enemy.getHp());
        assertEquals(30, enemy.getScoreValue());

        SimpleShotWeapon weapon = (SimpleShotWeapon) enemy.getWeapon();
        WeaponStats stats = weapon.getStats();
        assertEquals(3, stats.streamCount());
        assertEquals(19.0, stats.spreadWidth(), 1e-9);
        assertEquals(0.93, stats.cooldownInterval(), 1e-9);
    }

    @Test
    void defeatingDifferentEnemyTypesAwardsTheirConfiguredScores() {
        GamePanel panel = new GamePanel(false);

        Enemy fast = new FastEnemy(20, 20, 24, 24, 120, 1, new SimpleShotWeapon(1, 0, 1.0), 15);
        Enemy standard = new SimpleEnemy(60, 20, 30, 30, 100, 1, 30);
        Enemy heavy = new HeavyEnemy(110, 20, 48, 48, 80, 1, new SimpleShotWeapon(1, 0, 1.0), 60);

        panel.addEnemy(fast);
        panel.addEnemy(standard);
        panel.addEnemy(heavy);
        panel.addBullet(new Bullet(20, 20, 24, 24, 0, 0, true, 1));
        panel.addBullet(new Bullet(60, 20, 30, 30, 0, 0, true, 1));
        panel.addBullet(new Bullet(110, 20, 48, 48, 0, 0, true, 1));

        panel.checkCollisions();

        assertEquals(105, panel.getScore());
        assertEquals(105, panel.getBestScore());
    }

    @Test
    void reachingBossThresholdClearsRegularEnemiesAndSpawnsBoss() {
        GamePanel panel = new GamePanel(false);
        Enemy survivingEnemy = new SimpleEnemy(20, 20, 30, 30, 100, 2, 30);
        Enemy thresholdEnemy = new HeavyEnemy(80, 20, 40, 40, 70, 1, new SimpleShotWeapon(1, 0, 1.0), 100);

        panel.addEnemy(survivingEnemy);
        panel.addEnemy(thresholdEnemy);
        panel.addBullet(new Bullet(80, 20, 40, 40, 0, 0, true, 1));

        panel.checkCollisions();

        assertEquals(100, panel.getScore());
        assertEquals(1, panel.getEnemies().size());
        assertInstanceOf(BossEnemy.class, panel.getEnemies().get(0));
        assertFalse(panel.getEnemies().contains(survivingEnemy));
    }

    @Test
    void spawnLoopSkipsRegularEnemySpawnsWhileBossIsActive() {
        GamePanel panel = new GamePanel(false);

        panel.spawnBoss();
        panel.spawnEnemies(5.0);

        assertEquals(1, panel.getEnemies().size());
        assertTrue(panel.getEnemies().get(0) instanceof BossEnemy);
    }

    @Test
    void defeatingBossUpdatesNextBossThresholdToContinueWave() {
        GamePanel panel = new GamePanel(false);
        BossEnemy boss = new BossEnemy(150, 40);

        panel.addEnemy(boss);
        panel.addBullet(new Bullet(150, 40, 90, 90, 0, 0, true, 500));

        panel.checkCollisions();

        assertEquals(300, panel.getScore());
        assertEquals(400, panel.getNextBossScoreThreshold());
    }

    private static final class StubRandom extends Random {
        private final double[] doubles;
        private final int[] ints;
        private int doubleIndex;
        private int intIndex;

        private StubRandom(double[] doubles, int[] ints) {
            this.doubles = doubles;
            this.ints = ints;
        }

        @Override
        public double nextDouble() {
            if (doubleIndex >= doubles.length) {
                throw new AssertionError("Unexpected nextDouble() call");
            }

            return doubles[doubleIndex++];
        }

        @Override
        public int nextInt(int bound) {
            if (intIndex >= ints.length) {
                throw new AssertionError("Unexpected nextInt(int) call");
            }

            int value = ints[intIndex++];
            if (value < 0 || value >= bound) {
                throw new AssertionError("Stub nextInt value out of range: " + value + " for bound " + bound);
            }

            return value;
        }
    }
}