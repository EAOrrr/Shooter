public class Bullet extends GameObject {
    private final double speedX;
    private final double speedY;
    private final boolean fromPlayer;
    private final int damage;

    public Bullet(double x, double y, double speedX, double speedY, boolean fromPlayer, int damage) {
        this(x, y, 6, 12, speedX, speedY, fromPlayer, damage);
    }

    public Bullet(double x, double y, int width, int height, double speedX, double speedY, boolean fromPlayer, int damage) {
        super(x, y, width, height);
        this.speedX = speedX;
        this.speedY = speedY;
        this.fromPlayer = fromPlayer;
        this.damage = damage;
    }

    @Override
    public void update(double delta) {
        x += speedX * delta;
        y += speedY * delta;

        if (y < 0 || y > GamePanel.HEIGHT) {
            active = false;
        }
    }

    public boolean isFromPlayer() {
        return fromPlayer;
    }

    public int getDamage() {
        return damage;
    }
}
