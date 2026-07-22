import java.util.ArrayList;
import java.util.List;

public class SimpleShotWeapon implements UpgradableWeapon {
    private static final double PLAYER_BULLET_SPEED_Y = -400.0;
    private static final double ENEMY_BULLET_SPEED_Y = 400.0;

    private WeaponStats stats;
    private double cooldownTimer;

    public SimpleShotWeapon(int count, double maxWidth) {
        this(count, maxWidth, 0.2);
    }

    public SimpleShotWeapon(int count, double maxWidth, double cooldownInterval) {
        this(new WeaponStats(count, maxWidth, cooldownInterval, 1));
    }

    public SimpleShotWeapon(WeaponStats stats) {
        this.stats = stats;
    }

    @Override
    public void update(double delta) {
        if (delta <= 0) {
            return;
        }

        cooldownTimer += delta;
    }

    @Override
    public boolean canShoot() {
        return cooldownTimer >= stats.cooldownInterval();
    }

    @Override
    public List<Bullet> shoot(double x, double y, boolean isPlayer) {
        if (!canShoot()) {
            return List.of();
        }

        cooldownTimer = 0.0;

        List<Bullet> bullets = new ArrayList<>(stats.streamCount());
        double speedY = isPlayer ? PLAYER_BULLET_SPEED_Y : ENEMY_BULLET_SPEED_Y;

        if (stats.streamCount() == 1) {
            bullets.add(new Bullet(x, y, 0.0, speedY, isPlayer, stats.bulletDamage()));
            return bullets;
        }

        double step = stats.spreadWidth() / (stats.streamCount() - 1);
        double startX = x - stats.spreadWidth() / 2.0;
        for (int i = 0; i < stats.streamCount(); i++) {
            bullets.add(new Bullet(startX + i * step, y, 0.0, speedY, isPlayer, stats.bulletDamage()));
        }

        return bullets;
    }

    @Override
    public WeaponStats getStats() {
        return stats;
    }

    @Override
    public void applyStats(WeaponStats newStats) {
        double progress = Math.min(1.0, cooldownTimer / stats.cooldownInterval());
        stats = newStats;
        cooldownTimer = progress * stats.cooldownInterval();
    }

    public double getCooldownTimer() {
        return cooldownTimer;
    }
}
