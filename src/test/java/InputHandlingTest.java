import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Canvas;
import java.awt.event.KeyEvent;
import org.junit.jupiter.api.Test;

class InputHandlingTest {

    @Test
    void keyDownSetsVelocityVector() {
        Player player = new Player(100, 200, 40, 40, 100, 400, 600);
        KeyInput keyInput = new KeyInput(player);

        keyInput.keyPressed(new KeyEvent(new Canvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_A, 'A'));

        assertEquals(-KeyInput.SPEED_PX_PER_SEC, player.getSpeedX(), 1e-9);
        assertEquals(0.0, player.getSpeedY(), 1e-9);
    }

    @Test
    void keyUpClearsVelocityVector() {
        Player player = new Player(100, 200, 40, 40, 100, 400, 600);
        KeyInput keyInput = new KeyInput(player);

        keyInput.keyPressed(new KeyEvent(new Canvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_A, 'A'));
        keyInput.keyReleased(new KeyEvent(new Canvas(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_A, 'A'));

        assertEquals(0.0, player.getSpeedX(), 1e-9);
        assertEquals(0.0, player.getSpeedY(), 1e-9);
    }
}
