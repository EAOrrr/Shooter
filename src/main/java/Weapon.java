import java.util.List;

public interface Weapon {
    List<Bullet> shoot(double x, double y, boolean isPlayer);

    void update(double delta);

    boolean canShoot();
}
