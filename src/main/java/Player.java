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