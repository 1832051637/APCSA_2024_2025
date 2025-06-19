import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;

public class Game extends JFrame {
    public Game() {
        setTitle("彩色砖块游戏");
        setSize(600, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        add(new GamePanel());
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Game::new);
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener, MouseMotionListener {
    int ballX = 300, ballY = 300, ballSize = 20;
    int ballDX = 2, ballDY = 2;
    Color ballColor = Color.RED;
    ArrayList<Brick> bricks = new ArrayList<>();
    Timer timer;
    boolean gameOver = false;
    boolean success = false;
    boolean started = false;

    int paddleX = 250, paddleY = 540, paddleWidth = 100, paddleHeight = 15;
    int score = 5;
    BufferedImage bottomImage;  

    private Clip currentClip; 
    private boolean isSoundPlaying = false; 

    public GamePanel() {
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);
        addMouseMotionListener(this);
        initBricks();

        try {
            bottomImage = ImageIO.read(new File("keyboard.jpg"));  
        } catch (IOException e) {
            e.printStackTrace();  
        }

        timer = new Timer(10, this);
        timer.start();
        currentClip = null; 
    }

    void initBricks() {
        Color[] colors = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.MAGENTA};
        Random rand = new Random();
        int numCols = 10; 
        int brickSpacing = 4; 
        int panelWidth = 600; 
        int brickWidth = (panelWidth - (numCols + 1) * brickSpacing) / numCols;
        int brickHeight = 20;

        for (int row = 0; row < 5; row++) {
            int y = 50 + row * 30;
            for (int col = 0; col < numCols; col++) {
                int x = brickSpacing + col * (brickWidth + brickSpacing);
                Color color = colors[rand.nextInt(colors.length)];
                bricks.add(new Brick(x, y, brickWidth, brickHeight, color, row));
            }
        }
    }

    //AI帮助解决音轨重叠问题
    public void resetGame() {
        stopSound(); 
        ballX = 300;
        ballY = 300;
        ballDX = 2;
        ballDY = 2;
        ballColor = Color.RED;

        bricks.clear();
        initBricks();

        paddleX = 250;
        score = 5;
        gameOver = false;
        success = false;
        started = false;

        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(ballColor);
        g.fillOval(ballX, ballY, ballSize, ballSize);

        g.setColor(Color.BLACK);
        g.fillRect(paddleX, paddleY, paddleWidth, paddleHeight);

        for (Brick brick : bricks) {
            if (!brick.destroyed) {
                g.setColor(brick.color);
                g.fillRect(brick.x, brick.y, brick.width, brick.height);
            }
        }

        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Health: " + score, 470, 30);

        if (!started) {
            g.setColor(Color.BLUE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Press SPACE to start", 180, 400);
        }

        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("GAME OVER", 180, 400);
        }

        if (success) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("SUCCESS!", 200, 400);
        }

        if (bottomImage != null) {
            int imgWidth = 300;  
            int imgHeight = (int) ((double) imgWidth / bottomImage.getWidth() * bottomImage.getHeight());  

            if (imgWidth > getWidth()) {
                imgWidth = getWidth() - 20;  
                imgHeight = (int) ((double) imgWidth / bottomImage.getWidth() * bottomImage.getHeight());  
            }

            if (imgHeight > getHeight()) {
                imgHeight = getHeight() - 30;  
                imgWidth = (int) ((double) imgHeight / bottomImage.getHeight() * bottomImage.getWidth());  
            }

            int x = (getWidth() - imgWidth) / 2;  
            int y = getHeight() - imgHeight - 30;  

            g.drawImage(bottomImage, x, y, imgWidth, imgHeight, this);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (!started || gameOver || success) return;

        ballX += ballDX;
        ballY += ballDY;

        Rectangle ballRect = new Rectangle(ballX, ballY, ballSize, ballSize);
        Rectangle paddleRect = new Rectangle(paddleX, paddleY, paddleWidth, paddleHeight);

        if (ballRect.intersects(paddleRect)) {
            ballDY = -Math.abs(ballDY);
        }

        if (ballX <= 0 || ballX + ballSize >= getWidth()) {
            ballDX *= -1;
        }
        if (ballY <= 0) {
            ballDY *= -1;
        }

        if (ballY + ballSize >= getHeight()) {
            gameOver = true;
        }

        for (Brick brick : bricks) {
            if (!brick.destroyed) {
                Rectangle brickRect = new Rectangle(brick.x, brick.y, brick.width, brick.height);
                if (ballRect.intersects(brickRect)) {
                    if (brick.color.equals(ballColor)) {
                        brick.destroyed = true;
                        score++;
                    } else {
                        score--;
                    }
                    ballDY *= -1;
                    break;
                }
            }
        }

        if (score < 0) {
            gameOver = true;
        }

        success = bricks.stream().allMatch(b -> b.destroyed);

        repaint();
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_SPACE && !started) {
            started = true;
            playSound("Background.wav"); 
        }

        if (key == KeyEvent.VK_1) ballColor = Color.RED;
        if (key == KeyEvent.VK_2) ballColor = Color.ORANGE;
        if (key == KeyEvent.VK_3) ballColor = Color.YELLOW;
        if (key == KeyEvent.VK_4) ballColor = Color.GREEN;
        if (key == KeyEvent.VK_5) ballColor = Color.MAGENTA;
        if (key == KeyEvent.VK_R) {
            resetGame(); 
        }
    }

    public void mouseMoved(MouseEvent e) {
        paddleX = e.getX() - paddleWidth / 2;
        repaint();
    }

    public void mouseDragged(MouseEvent e) {}
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    // 修改后的音效播放方法（带资源释放）
    public void playSound(String filename) {
        stopSound(); // 播放前先停止可能存在的旧音效
        try {
            File audioFile = new File(filename);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            currentClip = AudioSystem.getClip();
            currentClip.open(audioStream);
            currentClip.loop(Clip.LOOP_CONTINUOUSLY); // 循环播放
            isSoundPlaying = true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("音效文件加载失败：" + filename);
        }
    }

    // 新增音效停止方法
    private void stopSound() {
        if (currentClip != null && currentClip.isOpen()) {
            currentClip.stop(); // 停止播放
            currentClip.close(); // 关闭音频流
            currentClip = null;
            isSoundPlaying = false;
        }
    }
}

class Brick {
    int x, y, width, height;
    Color color;
    int row;
    boolean destroyed = false;

    public Brick(int x, int y, int width, int height, Color color, int row) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.row = row;
    }
}