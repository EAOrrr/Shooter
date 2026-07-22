import java.util.List;

public abstract class AirPlane extends GameObject {
    protected int hp;
    protected int maxHp;
    protected Weapon weapon;

    protected AirPlane(double x, double y, int width, int height, int maxHp, int initialHp, Weapon weapon) {
        super(x, y, width, height);

        if (maxHp <= 0) {
            throw new IllegalArgumentException("maxHp must be greater than 0");
        }

        this.maxHp = maxHp;
        this.hp = Math.max(0, Math.min(initialHp, maxHp));
        this.weapon = weapon;

        if (this.hp == 0) {
            this.active = false;
        }
    }

    public List<Bullet> shoot() {
        if (weapon == null) {
            return List.of();
        }

        return weapon.shoot(x + width / 2.0, y, isPlayer());
    }

    public Weapon getWeapon() {
        return weapon;
    }

    protected abstract boolean isPlayer();

    public void takeDamage(int damage) {
        if (damage <= 0 || hp <= 0) {
            return;
        }

        hp = Math.max(0, hp - damage);
        if (hp == 0) {
            active = false;
        }
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }
}