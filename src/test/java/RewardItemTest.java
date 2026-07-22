import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

class RewardItemTest {

    @Test
    void rewardSpawnLoopCreatesFallingItemsAfterEnoughDelta() {
        GamePanel panel = new GamePanel(false);

        panel.spawnRewards(6.0);

        assertFalse(panel.getRewardItems().isEmpty());
        assertTrue(panel.getRewardItems().get(0).isActive());
    }

    @Test
    void healRewardScalesWithScoreAndRestoresPlayerHp() {
        GamePanel lowScorePanel = new GamePanel(false, new FixedRandom());
        GamePanel highScorePanel = new GamePanel(false, new FixedRandom());

        lowScorePanel.getPlayer().takeDamage(400);
        highScorePanel.getPlayer().takeDamage(400);
        highScorePanel.addScoreForTesting(2000);

        RewardItem lowHeal = new RewardItem(
            lowScorePanel.getPlayer().getX(),
            lowScorePanel.getPlayer().getY(),
            lowScorePanel.getPlayer().getWidth(),
            lowScorePanel.getPlayer().getHeight(),
            0.0,
            RewardItem.RewardType.HEAL
        );
        RewardItem highHeal = new RewardItem(
            highScorePanel.getPlayer().getX(),
            highScorePanel.getPlayer().getY(),
            highScorePanel.getPlayer().getWidth(),
            highScorePanel.getPlayer().getHeight(),
            0.0,
            RewardItem.RewardType.HEAL
        );

        int lowBefore = lowScorePanel.getPlayer().getHp();
        int highBefore = highScorePanel.getPlayer().getHp();

        lowScorePanel.addRewardItem(lowHeal);
        highScorePanel.addRewardItem(highHeal);
        lowScorePanel.checkCollisions();
        highScorePanel.checkCollisions();

        int lowRecovered = lowScorePanel.getPlayer().getHp() - lowBefore;
        int highRecovered = highScorePanel.getPlayer().getHp() - highBefore;

        assertFalse(lowHeal.isActive());
        assertFalse(highHeal.isActive());
        assertTrue(lowRecovered > 0);
        assertTrue(highRecovered > lowRecovered);
    }

    @Test
    void weaponRewardsImproveCooldownStreamCountAndDamage() {
        GamePanel panel = new GamePanel(false, new FixedRandom());
        UpgradableWeapon weapon = (UpgradableWeapon) panel.getPlayer().getWeapon();

        double initialCooldown = weapon.getStats().cooldownInterval();
        int initialStreams = weapon.getStats().streamCount();
        double initialSpreadWidth = weapon.getStats().spreadWidth();
        int initialDamage = weapon.getStats().bulletDamage();

        panel.addRewardItem(new RewardItem(panel.getPlayer().getX(), panel.getPlayer().getY(), 24, 24, 0.0, RewardItem.RewardType.RAPID_FIRE));
        panel.checkCollisions();

        panel.addRewardItem(new RewardItem(panel.getPlayer().getX(), panel.getPlayer().getY(), 24, 24, 0.0, RewardItem.RewardType.MULTI_SHOT));
        panel.checkCollisions();

        panel.addRewardItem(new RewardItem(panel.getPlayer().getX(), panel.getPlayer().getY(), 24, 24, 0.0, RewardItem.RewardType.POWER_SHOT));
        panel.checkCollisions();

        assertTrue(weapon.getStats().cooldownInterval() < initialCooldown);
        assertTrue(weapon.getStats().streamCount() > initialStreams);
        assertTrue(weapon.getStats().spreadWidth() > initialSpreadWidth);
        assertTrue(weapon.getStats().bulletDamage() > initialDamage);
    }

    @Test
    void multiShotDropRateIsVeryRare() {
        Random random = new CyclingHundredRandom();
        int multiShotCount = 0;
        int expectedPerHundred = RewardItem.multiShotChancePercent();

        for (int i = 0; i < 100; i++) {
            if (RewardItem.randomType(random) == RewardItem.RewardType.MULTI_SHOT) {
                multiShotCount++;
            }
        }

        assertEquals(expectedPerHundred, multiShotCount);
    }

    private static final class FixedRandom extends Random {
        @Override
        public double nextDouble() {
            return 0.0;
        }

        @Override
        public int nextInt(int bound) {
            return 0;
        }
    }

    private static final class CyclingHundredRandom extends Random {
        private int value;

        @Override
        public int nextInt(int bound) {
            if (bound != 100) {
                return super.nextInt(bound);
            }

            int next = value;
            value = (value + 1) % 100;
            return next;
        }
    }
}