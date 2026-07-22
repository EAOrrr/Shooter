public class Player extends AirPlane {
    private double speedX;
    private double speedY;
    private final int worldWidth;
    private final int worldHeight;

    public Player(double x, double y, int width, int height, int maxHp, int worldWidth, int worldHeight) {
        super(x, y, width, height, maxHp, maxHp);

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
}