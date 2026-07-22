import java.awt.Color;
import java.awt.Graphics;

public class Player extends AirPlane {
    private static final double STREAM_SPREAD_STEP = 8.0;
    private static final double MAX_STREAM_SPREAD_WIDTH_FACTOR = 1.2;

    private double speedX;
    private double speedY;
    private final int worldWidth;
    private final int worldHeight;
    private final double spawnX;
    private final double spawnY;
    private boolean invincible;

    public Player(double x, double y, int width, int height, int maxHp, int worldWidth, int worldHeight) {
        this(x, y, width, height, maxHp, worldWidth, worldHeight, new SimpleShotWeapon(new WeaponStats(1, 0.0, 0.2, 1)));
    }

    public Player(double x, double y, int width, int height, int maxHp, int worldWidth, int worldHeight, Weapon weapon) {
        super(x, y, width, height, maxHp, maxHp, weapon);

        this.spawnX = x;
        this.spawnY = y;
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

    public void upgradeWeapon() {
        if (weapon instanceof SpreadWeapon) {
            return;
        }
        weapon = new SpreadWeapon(3, 30);
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
        int addedStreams = nextStreams - current.streamCount();
        double nextSpreadWidth = Math.min(getMaxStreamSpreadWidth(), current.spreadWidth() + addedStreams * STREAM_SPREAD_STEP);
        upgradableWeapon.applyStats(new WeaponStats(nextStreams, nextSpreadWidth, current.cooldownInterval(), current.bulletDamage()));
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

    public boolean isInvincible() {
        return invincible;
    }

    public void setInvincible(boolean invincible) {
        this.invincible = invincible;
    }

    public void restoreHp(int amount) {
        if (amount <= 0 || hp <= 0) {
            return;
        }

        hp = Math.min(maxHp, hp + amount);
    }

    void resetToSpawn() {
        x = spawnX;
        y = spawnY;
        speedX = 0.0;
        speedY = 0.0;
        hp = maxHp;
        active = true;
        invincible = false;
        weapon = new SimpleShotWeapon(new WeaponStats(1, 0.0, 0.2, 1));
    }

    private UpgradableWeapon getUpgradableWeapon() {
        if (weapon instanceof UpgradableWeapon upgradableWeapon) {
            return upgradableWeapon;
        }
        return null;
    }

    private double getMaxStreamSpreadWidth() {
        return width * MAX_STREAM_SPREAD_WIDTH_FACTOR;
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
    public void takeDamage(int damage) {
        if (invincible) {
            return;
        }
        super.takeDamage(damage);
    }

    @Override
    protected boolean isPlayer() {
        return true;
    }
}