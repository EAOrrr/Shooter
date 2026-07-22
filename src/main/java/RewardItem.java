import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

public class RewardItem extends GameObject {
    enum RewardType {
        HEAL,
        RAPID_FIRE,
        MULTI_SHOT,
        POWER_SHOT
    }

    private static final int BASE_HEAL_MIN = 8;
    private static final int BASE_HEAL_MAX = 16;
    private static final int HEAL_SCORE_STEP = 500;
    private static final int HEAL_ROLL_MAX_EXCLUSIVE = 45;
    private static final int RAPID_FIRE_ROLL_MAX_EXCLUSIVE = 75;
    private static final int POWER_SHOT_ROLL_MAX_EXCLUSIVE = 90;

    private final double fallSpeed;
    private final RewardType rewardType;

    public RewardItem(double x, double y, int width, int height, double fallSpeed, RewardType rewardType) {
        super(x, y, width, height);

        this.fallSpeed = fallSpeed;
        this.rewardType = rewardType;
    }

    public RewardType getRewardType() {
        return rewardType;
    }

    @Override
    public void update(double delta) {
        if (delta <= 0 || !active) {
            return;
        }

        y += fallSpeed * delta;
        if (y > GamePanel.HEIGHT) {
            active = false;
        }
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(colorForType());
        g.fillOval((int) Math.round(x), (int) Math.round(y), width, height);
    }

    public void applyTo(Player player, int score, Random random) {
        switch (rewardType) {
            case HEAL -> player.restoreHp(rollHealAmount(score, random));
            case RAPID_FIRE -> {
                int tier = score / 1000;
                double factor = Math.max(0.70, 0.90 - tier * 0.02);
                player.upgradeWeaponCooldown(factor, 0.06);
            }
            case MULTI_SHOT -> {
                int increment = 1;
                player.upgradeWeaponStreams(increment, 8);
            }
            case POWER_SHOT -> {
                int increment = 1 + score / 3000;
                player.upgradeWeaponDamage(increment, 10);
            }
        }
    }

    static RewardType randomType(Random random) {
        int roll = random.nextInt(100);
        if (roll < HEAL_ROLL_MAX_EXCLUSIVE) {
            return RewardType.HEAL;
        }
        if (roll < RAPID_FIRE_ROLL_MAX_EXCLUSIVE) {
            return RewardType.RAPID_FIRE;
        }
        if (roll < POWER_SHOT_ROLL_MAX_EXCLUSIVE) {
            return RewardType.POWER_SHOT;
        }
        return RewardType.MULTI_SHOT;
    }

    static int multiShotChancePercent() {
        return 100 - POWER_SHOT_ROLL_MAX_EXCLUSIVE;
    }

    int rollHealAmount(int score, Random random) {
        int scoreSteps = Math.max(0, score / HEAL_SCORE_STEP);
        int min = BASE_HEAL_MIN + scoreSteps;
        int max = BASE_HEAL_MAX + scoreSteps * 3;
        return min + random.nextInt(max - min + 1);
    }

    private Color colorForType() {
        return switch (rewardType) {
            case HEAL -> new Color(80, 220, 120);
            case RAPID_FIRE -> new Color(255, 210, 50);
            case MULTI_SHOT -> new Color(100, 180, 255);
            case POWER_SHOT -> new Color(255, 120, 80);
        };
    }
}