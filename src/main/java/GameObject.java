import java.awt.Graphics;
import java.awt.Rectangle;

public abstract class GameObject {
    protected double x;
    protected double y;
    protected int width;
    protected int height;
    protected boolean active;

    protected GameObject(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.active = true;
    }

    public abstract void update(double delta);

    public abstract void draw(Graphics g);

    public Rectangle getBounds() {
        return new Rectangle((int) Math.round(x), (int) Math.round(y), width, height);
    }

    public boolean intersects(GameObject other) {
        return getBounds().intersects(other.getBounds());
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}