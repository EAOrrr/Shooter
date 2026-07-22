import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlaneGame extends JPanel implements ActionListener, KeyListener {
    // 窗口尺寸
    private static final int WIDTH = 400;
    private static final int HEIGHT = 600;

    // 玩家飞机属性
    private int playerX = 180;
    private int playerY = 500;
    private final int playerWidth = 40;
    private final int playerHeight = 40;
    private boolean left, right, up, down, shooting;

    // 子弹与敌机列表 (用 Rectangle 方便碰撞检测)
    private final List<Rectangle> bullets = new ArrayList<>();
    private final List<Rectangle> enemies = new ArrayList<>();

    // 游戏状态
    private int score = 0;
    private boolean gameOver = false;
    private final Timer timer;
    private int spawnCounter = 0;
    private int shootCooldown = 0;

    public PlaneGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        // 主循环定时器，约 60 帧/秒 (16 ms)
        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameOver) {
            // 绘制 Game Over 画面
            g.setColor(Color.WHITE);
            g.setFont(new Font("Microsoft YaHei", Font.BOLD, 32));
            g.drawString("GAME OVER", 110, 260);

            g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
            g.drawString("最终得分: " + score, 150, 310);
            g.drawString("按 R 键重新开始", 135, 350);
            return;
        }

        // 1. 绘制玩家（绿/蓝色机体）
        g.setColor(Color.CYAN);
        // 机身
        g.fillRect(playerX + 15, playerY, 10, 40);
        // 机翼
        g.fillRect(playerX, playerY + 20, 40, 8);

        // 2. 绘制子弹（黄色）
        g.setColor(Color.YELLOW);
        for (Rectangle b : bullets) {
            g.fillRect(b.x, b.y, b.width, b.height);
        }

        // 3. 绘制敌机（红色）
        g.setColor(Color.RED);
        for (Rectangle e : enemies) {
            g.fillRect(e.x, e.y, e.width, e.height);
        }

        // 4. 绘制得分
        g.setColor(Color.WHITE);
        g.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        g.drawString("得分: " + score, 15, 25);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        // --- 玩家移动 ---
        int speed = 5;
        if (left && playerX > 0) playerX -= speed;
        if (right && playerX < WIDTH - playerWidth) playerX += speed;
        if (up && playerY > 0) playerY -= speed;
        if (down && playerY < HEIGHT - playerHeight) playerY += speed;

        // --- 玩家发弹 ---
        if (shootCooldown > 0) shootCooldown--;
        if (shooting && shootCooldown == 0) {
            // 从机头位置发射子弹
            bullets.add(new Rectangle(playerX + 18, playerY - 10, 4, 10));
            shootCooldown = 10; // 发射冷却间隔
        }

        // --- 移动子弹 ---
        Iterator<Rectangle> bIter = bullets.iterator();
        while (bIter.hasNext()) {
            Rectangle b = bIter.next();
            b.y -= 8; // 子弹速度
            if (b.y < 0) bIter.remove(); // 飞出屏幕销毁
        }

        // --- 生成敌机 ---
        spawnCounter++;
        if (spawnCounter % 35 == 0) { // 约每 0.5 秒生成一架
            int rx = (int) (Math.random() * (WIDTH - 30));
            enemies.add(new Rectangle(rx, -30, 30, 30));
        }

        // --- 移动敌机与玩家碰撞检测 ---
        Rectangle playerBounds = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        Iterator<Rectangle> eIter = enemies.iterator();
        while (eIter.hasNext()) {
            Rectangle enemy = eIter.next();
            enemy.y += 3; // 敌机下落速度

            // 撞到玩家
            if (enemy.intersects(playerBounds)) {
                gameOver = true;
            }

            // 飞出底部销毁
            if (enemy.y > HEIGHT) {
                eIter.remove();
            }
        }

        // --- 子弹与敌机碰撞检测 ---
        bIter = bullets.iterator();
        while (bIter.hasNext()) {
            Rectangle b = bIter.next();
            eIter = enemies.iterator();
            while (eIter.hasNext()) {
                Rectangle enemy = eIter.next();
                if (b.intersects(enemy)) {
                    bIter.remove();
                    eIter.remove();
                    score += 10;
                    break;
                }
            }
        }

        repaint();
    }

    // 重置游戏
    private void resetGame() {
        playerX = 180;
        playerY = 500;
        bullets.clear();
        enemies.clear();
        score = 0;
        gameOver = false;
        left = right = up = down = shooting = false;
    }

    // --- 按键监听 ---
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) left = true;
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) right = true;
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) up = true;
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) down = true;
        if (key == KeyEvent.VK_SPACE) shooting = true;

        if (gameOver && key == KeyEvent.VK_R) {
            resetGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) left = false;
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) right = false;
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) up = false;
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) down = false;
        if (key == KeyEvent.VK_SPACE) shooting = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    // 启动入口
    public static void main(String[] args) {
        JFrame frame = new JFrame("简易打飞机");
        PlaneGame game = new PlaneGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}