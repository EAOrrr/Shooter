public class BossEnemy extends Enemy {
    public static final int DEFAULT_WIDTH = 90;
    public static final int DEFAULT_HEIGHT = 90;
    public static final int DEFAULT_MAX_HP = 500;

    private static final int PHASE_TWO_HP_THRESHOLD = 250;
    private static final double OSCILLATION_SPEED = 1.6;
    private static final double OSCILLATION_AMPLITUDE = 120.0;

    private final double anchorX;
    private double timeAccumulator;
    private int phase;

    public BossEnemy(double x, double y) {
        super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_MAX_HP, DEFAULT_MAX_HP, new SimpleShotWeapon(1, 0, 0.8), 300);
        this.anchorX = x;
        this.phase = 1;
    }

    @Override
    public void update(double delta) {
        if (delta <= 0) {
            return;
        }

        timeAccumulator += delta;

        // Combine sine and cosine on accumulated delta time for smooth horizontal movement.
        double wave = Math.sin(timeAccumulator * OSCILLATION_SPEED) + 0.35 * Math.cos(timeAccumulator * OSCILLATION_SPEED * 0.5);
        x = anchorX + wave * OSCILLATION_AMPLITUDE;

        double minX = 0.0;
        double maxX = Math.max(0.0, GamePanel.WIDTH - width);
        x = Math.max(minX, Math.min(x, maxX));

        if (phase == 1 && hp < PHASE_TWO_HP_THRESHOLD) {
            weapon = new SpreadWeapon(5, 40, 450.0, 0.45);
            phase = 2;
        }
    }

    public int getPhase() {
        return phase;
    }

    public double getTimeAccumulator() {
        return timeAccumulator;
    }
}
