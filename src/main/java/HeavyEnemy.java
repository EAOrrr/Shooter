import java.awt.Color;
import java.awt.Graphics;

public class HeavyEnemy extends Enemy {
    private final double speedY;

    public HeavyEnemy(double x, double y, int width, int height, double speedY, int hp, Weapon weapon, int scoreValue) {
        super(x, y, width, height, hp, hp, weapon, scoreValue);
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

        g.setColor(Color.MAGENTA);
        g.fillRoundRect(drawX, drawY, width, height, 10, 10);

        g.setColor(Color.WHITE);
        g.drawString(String.valueOf(hp), drawX + width / 2 - 4, drawY + height / 2 + 4);
    }
}