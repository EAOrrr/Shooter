import java.awt.Color;
import java.awt.Graphics;

public class Player extends AirPlane {
    private double speedX;
    private double speedY;
    private final int worldWidth;
    private final int worldHeight;

    public Player(double x, double y, int width, int height, int maxHp, int worldWidth, int worldHeight) {
        this(x, y, width, height, maxHp, worldWidth, worldHeight, new SimpleShotWeapon(3, 20));
    }

    public Player(double x, double y, int width, int height, int maxHp, int worldWidth, int worldHeight, Weapon weapon) {
        super(x, y, width, height, maxHp, maxHp, weapon);

        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    public void setSpeed(double speedX, double speedY) {
        this.speedX = speedX;
        this.speedY = speedY;
    }

    public double getSpeedX() {
        return speedX;
    }

    public double getSpeedY() {
        return speedY;
    }

    public void upgradeWeaponStreams(int increment, int maxStreams) {
        if (increment <= 0) {
            return;
        }

        UpgradableWeapon upgradableWeapon = getUpgradableWeapon();
        if (upgradableWeapon == null) {
            return;
        }

        WeaponStats current = upgradableWeapon.getStats();
        int nextStreams = Math.min(maxStreams, current.streamCount() + increment);
        upgradableWeapon.applyStats(new WeaponStats(nextStreams, current.spreadWidth(), current.cooldownInterval(), current.bulletDamage()));
    }

    public void upgradeWeaponCooldown(double factor, double minCooldown) {
        if (factor <= 0) {
            return;
        }

        UpgradableWeapon upgradableWeapon = getUpgradableWeapon();
        if (upgradableWeapon == null) {
            return;
        }

        WeaponStats current = upgradableWeapon.getStats();
        double nextCooldown = Math.max(minCooldown, current.cooldownInterval() * factor);
        upgradableWeapon.applyStats(new WeaponStats(current.streamCount(), current.spreadWidth(), nextCooldown, current.bulletDamage()));
    }

    public void upgradeWeaponDamage(int increment, int maxDamage) {
        if (increment <= 0) {
            return;
        }

        UpgradableWeapon upgradableWeapon = getUpgradableWeapon();
        if (upgradableWeapon == null) {
            return;
        }

        WeaponStats current = upgradableWeapon.getStats();
        int nextDamage = Math.min(maxDamage, current.bulletDamage() + increment);
        upgradableWeapon.applyStats(new WeaponStats(current.streamCount(), current.spreadWidth(), current.cooldownInterval(), nextDamage));
    }

    private UpgradableWeapon getUpgradableWeapon() {
        if (weapon instanceof UpgradableWeapon upgradableWeapon) {
            return upgradableWeapon;
        }
        return null;
    }

    @Override
    public void update(double delta) {
        x += speedX * delta;
        y += speedY * delta;

        double maxX = Math.max(0, worldWidth - width);
        double maxY = Math.max(0, worldHeight - height);
        x = Math.max(0, Math.min(x, maxX));
        y = Math.max(0, Math.min(y, maxY));
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.CYAN);
        g.fillRect((int) Math.round(x), (int) Math.round(y), width, height);
    }

    @Override
    protected boolean isPlayer() {
        return true;
    }
}