import java.awt.Color;
import java.awt.Graphics;

public abstract class Enemy extends AirPlane {
    private final int scoreValue;

    protected Enemy(double x, double y, int width, int height, int maxHp, int initialHp, Weapon weapon, int scoreValue) {
        super(x, y, width, height, maxHp, initialHp, weapon);
        this.scoreValue = scoreValue;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    @Override
    protected boolean isPlayer() {
        return false;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect((int) Math.round(x), (int) Math.round(y), width, height);
    }
}
