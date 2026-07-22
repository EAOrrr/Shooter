import java.awt.Color;
import java.awt.Graphics;

public class SimpleEnemy extends Enemy {
    private final double speedY;

    public SimpleEnemy(double x, double y, double speedY) {
        this(x, y, 30, 30, speedY, 1, 10);
    }

    public SimpleEnemy(double x, double y, double speedY, int hp) {
        this(x, y, 30, 30, speedY, hp, 10);
    }

    public SimpleEnemy(double x, double y, int width, int height, double speedY, int hp, int scoreValue) {
        super(x, y, width, height, hp, hp, new SimpleShotWeapon(1, 0, 1.2), scoreValue);
        this.speedY = speedY;
    }

    @Override
    public void update(double delta) {
        y += speedY * delta;
        if (y > GamePanel.HEIGHT + 10) {
            active = false;
        }
    }

    @Override
    public void draw(Graphics g) {
        int drawX = (int) Math.round(x);
        int drawY = (int) Math.round(y);

        g.setColor(Color.RED);
        g.fillRect(drawX, drawY, width, height);

        g.setColor(Color.WHITE);
        g.drawString(String.valueOf(hp), drawX + width / 2 - 4, drawY + height / 2 + 4);
    }
}
