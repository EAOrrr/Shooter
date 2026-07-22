import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics;
import org.junit.jupiter.api.Test;

class CollisionTest {

    @Test
    void intersectsReturnsTrueForOverlappingBoxes() {
        TestObject boxA = new TestObject(0, 0, 20, 20, 10, 5);
        TestObject boxB = new TestObject(10, 10, 20, 20, 0, 0);

        assertTrue(boxA.intersects(boxB));
    }

    @Test
    void intersectsReturnsFalseForSeparatedBoxes() {
        TestObject boxA = new TestObject(0, 0, 20, 20, 10, 5);
        TestObject boxB = new TestObject(50, 50, 20, 20, 0, 0);

        assertFalse(boxA.intersects(boxB));
    }

    @Test
    void updateWithZeroDeltaKeepsPositionStable() {
        TestObject gameObject = new TestObject(12, 34, 20, 20, 120, -80);
        double initialX = gameObject.getX();
        double initialY = gameObject.getY();

        assertDoesNotThrow(() -> gameObject.update(0.0));
        assertTrue(initialX == gameObject.getX());
        assertTrue(initialY == gameObject.getY());
    }

    private static final class TestObject extends GameObject {
        private final double velocityX;
        private final double velocityY;

        private TestObject(double x, double y, int width, int height, double velocityX, double velocityY) {
            super(x, y, width, height);
            this.velocityX = velocityX;
            this.velocityY = velocityY;
        }

        @Override
        public void update(double delta) {
            x += velocityX * delta;
            y += velocityY * delta;
        }

        @Override
        public void draw(Graphics g) {
            // No-op: rendering is not relevant to collision behavior tests.
        }
    }
}