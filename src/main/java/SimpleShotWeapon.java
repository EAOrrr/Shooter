import java.util.ArrayList;
import java.util.List;

public class SimpleShotWeapon implements Weapon {
    private static final double PLAYER_BULLET_SPEED_Y = -400.0;
    private static final double ENEMY_BULLET_SPEED_Y = 400.0;

    private final int count;
    private final double maxWidth;
    private final double cooldownInterval;
    private double cooldownTimer;

    public SimpleShotWeapon(int count, double maxWidth) {
        this(count, maxWidth, 0.2);
    }

    public SimpleShotWeapon(int count, double maxWidth, double cooldownInterval) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be greater than 0");
        }
        if (maxWidth < 0) {
            throw new IllegalArgumentException("maxWidth must be greater than or equal to 0");
        }
        if (cooldownInterval <= 0) {
            throw new IllegalArgumentException("cooldownInterval must be greater than 0");
        }

        this.count = count;
        this.maxWidth = maxWidth;
        this.cooldownInterval = cooldownInterval;
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
        return cooldownTimer >= cooldownInterval;
    }

    @Override
    public List<Bullet> shoot(double x, double y, boolean isPlayer) {
        if (!canShoot()) {
            return List.of();
        }

        cooldownTimer = 0.0;

        List<Bullet> bullets = new ArrayList<>(count);
        double speedY = isPlayer ? PLAYER_BULLET_SPEED_Y : ENEMY_BULLET_SPEED_Y;

        if (count == 1) {
            bullets.add(new Bullet(x, y, 0.0, speedY, isPlayer, 1));
            return bullets;
        }

        double step = maxWidth / (count - 1);
        double startX = x - maxWidth / 2.0;
        for (int i = 0; i < count; i++) {
            bullets.add(new Bullet(startX + i * step, y, 0.0, speedY, isPlayer, 1));
        }

        return bullets;
    }

    public double getCooldownTimer() {
        return cooldownTimer;
    }
}
