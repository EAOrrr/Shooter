import java.util.ArrayList;
import java.util.List;

public class SpreadWeapon implements Weapon {
    private static final double PLAYER_DIRECTION = -1.0;
    private static final double ENEMY_DIRECTION = 1.0;
    private static final double DEFAULT_BULLET_SPEED = 500.0;
    private static final double DEFAULT_COOLDOWN = 0.2;
    private static final int DEFAULT_DAMAGE = 1;

    private final int count;
    private final double maxAngle;
    private final double bulletSpeed;
    private final double cooldownInterval;
    private double cooldownTimer;

    public SpreadWeapon(int count, double maxAngle) {
        this(count, maxAngle, DEFAULT_BULLET_SPEED, DEFAULT_COOLDOWN);
    }

    public SpreadWeapon(int count, double maxAngle, double bulletSpeed, double cooldownInterval) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be greater than 0");
        }
        if (maxAngle < 0) {
            throw new IllegalArgumentException("maxAngle must be greater than or equal to 0");
        }
        if (bulletSpeed <= 0) {
            throw new IllegalArgumentException("bulletSpeed must be greater than 0");
        }
        if (cooldownInterval <= 0) {
            throw new IllegalArgumentException("cooldownInterval must be greater than 0");
        }

        this.count = count;
        this.maxAngle = maxAngle;
        this.bulletSpeed = bulletSpeed;
        this.cooldownInterval = cooldownInterval;
    }

    @Override
    public List<Bullet> shoot(double x, double y, boolean isPlayer) {
        if (!canShoot()) {
            return List.of();
        }

        cooldownTimer = 0.0;
        List<Bullet> bullets = new ArrayList<>(count);

        if (count == 1) {
            double speedY = (isPlayer ? PLAYER_DIRECTION : ENEMY_DIRECTION) * bulletSpeed;
            bullets.add(new Bullet(x, y, 0.0, speedY, isPlayer, DEFAULT_DAMAGE));
            return bullets;
        }

        double start = -maxAngle / 2.0;
        double step = maxAngle / (count - 1);
        double yDirection = isPlayer ? PLAYER_DIRECTION : ENEMY_DIRECTION;

        for (int i = 0; i < count; i++) {
            double angleDeg = start + i * step;
            double angleRad = Math.toRadians(angleDeg);
            double speedX = bulletSpeed * Math.sin(angleRad);
            double speedY = yDirection * bulletSpeed * Math.cos(angleRad);
            bullets.add(new Bullet(x, y, speedX, speedY, isPlayer, DEFAULT_DAMAGE));
        }

        return bullets;
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
}