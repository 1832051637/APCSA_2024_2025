import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 600;
    private static final int PLANE_WIDTH = 75;
    private static final int PLANE_HEIGHT = 49;
    private static final int GRAVITY = 2;
    private static final int JUMP_STRENGTH = 14;
    private static final int MIN_PIPE_WIDTH = 40; 
    private static final int MAX_PIPE_WIDTH = 60;
    private static final int MIN_GAP = 100; 
    private static final int MAX_GAP = 250; 
    private static final int PIPE_SPEED = 10;
    private static final int MIN_SPAWN_DELAY = 20; 
    private static final int MAX_SPAWN_DELAY = 70; 

    private int planeY;
    private int planeVelocity;
    private ArrayList<Rectangle> pipes;
    private Timer timer;
    private Random random;
    private boolean gameOver;
    private boolean gameStarted;
    private int score;
    private int pipeSpawnCounter;
    private int nextSpawnDelay;
    private Clip backgroundClip;
    private boolean showCover = true; // 新增：封面显示状态
    private BufferedImage backgroundImage;
    private String backgroundPath = "Resource/images/background.png";
    private BufferedImage playerImage;
    private String playerPath = "Resource/images/plane.png";



    public FlappyBird() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.CYAN);
        setFocusable(true);
        addKeyListener(this);
        loadBackground();
        loadPlayer();
        loadbgm("Resource/sounds/bgm.wav");
        planeY = HEIGHT / 2;
        planeVelocity = 0;
        pipes = new ArrayList<>();
        random = new Random();
        gameOver = false;
        gameStarted = false;
        score = 0;
        pipeSpawnCounter = 0;
        nextSpawnDelay = random.nextInt(MAX_SPAWN_DELAY - MIN_SPAWN_DELAY) + MIN_SPAWN_DELAY;
        timer = new Timer(20, this);
    }

    public void loadBackground(){
        try {
            backgroundImage = ImageIO.read(new File(backgroundPath));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void loadPlayer() {
        try{
            playerImage = ImageIO.read(new File(playerPath));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void generatePipe() {
        int pipeWidth = random.nextInt(MAX_PIPE_WIDTH - MIN_PIPE_WIDTH) + MIN_PIPE_WIDTH;
        int gap = random.nextInt(MAX_GAP - MIN_GAP) + MIN_GAP;
        int topHeight = random.nextInt(HEIGHT - gap);

        pipes.add(new Rectangle(WIDTH, 0, pipeWidth, topHeight)); // 上方柱子
        pipes.add(new Rectangle(WIDTH, topHeight + gap, pipeWidth, HEIGHT - topHeight - gap)); // 下方柱子

        // 随机生成下一个柱子的生成间隔
        nextSpawnDelay = random.nextInt(MAX_SPAWN_DELAY - MIN_SPAWN_DELAY) + MIN_SPAWN_DELAY;
    }

    private void movePipes() {
        for (Rectangle pipe : pipes) {
            pipe.x -= PIPE_SPEED;
        }
        if (!pipes.isEmpty() && pipes.get(0).x + pipes.get(0).width < 0) {
            pipes.remove(0);
            pipes.remove(0);
            score++;
        }
    }

    private void checkCollisions() {
        Rectangle planeRect = new Rectangle(WIDTH / 2, planeY+PLANE_HEIGHT/2, PLANE_WIDTH/2, PLANE_HEIGHT/2);
        for (Rectangle pipe : pipes) {
            if (planeRect.intersects(pipe)) {
                gameOver = true;
                timer.stop();
                stopbgm();
                break;
            }
        }
        if (planeY + PLANE_HEIGHT > HEIGHT || planeY < 0) {
            gameOver = true;
            timer.stop();
            stopbgm();
        }
    }

    private void resetGame() {
        planeY = HEIGHT / 2;
        planeVelocity = 0;
        pipes.clear();
        gameOver = false;
        score = 0;
        pipeSpawnCounter = 0;
        nextSpawnDelay = random.nextInt(MAX_SPAWN_DELAY - MIN_SPAWN_DELAY) + MIN_SPAWN_DELAY;
        timer.stop();
        gameStarted =  true;
        timer.start();
        loopbgm();
        showCover = false; // 重置时不显示封面
    }
    

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
 

        // 绘制游戏封面
        if (showCover) {
            drawCover(g);
            return;
        }
        g.drawImage(backgroundImage, 0, 0, null);
        // Draw plane
        // g.setColor(Color.YELLOW);
        // g.fillRect(WIDTH / 2 - PLANE_WIDTH / 2, planeY, PLANE_WIDTH, PLANE_HEIGHT);


        g.drawImage(playerImage, WIDTH/2 - PLANE_WIDTH/2, planeY, null);

        //绘制hitbox
        //g.drawRect(WIDTH / 2, planeY+PLANE_HEIGHT/2, PLANE_WIDTH/2, PLANE_HEIGHT/2);

        // Draw pipes
        g.setColor(Color.GRAY);
        for (Rectangle pipe : pipes) {
            g.fillRect(pipe.x, pipe.y, pipe.width, pipe.height);
        }

        // Draw score
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Score: " + score, 20, 30);

        if (!gameStarted) {
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("Press SPACE to Start", WIDTH / 2 - 220, HEIGHT / 2);
        }

        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("Game Over!", WIDTH / 2 - 120, HEIGHT / 2);
            g.drawString("Press SPACE to Restart", WIDTH / 2 - 220, HEIGHT / 2 + 60);
        }
    }

    // 新增：绘制游戏封面
    private void drawCover(Graphics g) {
        // 封面背景
        // g.setColor(new Color(135, 206, 235)); // 天空蓝
        // g.fillRect(0, 0, WIDTH, HEIGHT);
        
        g.drawImage(backgroundImage, 0, 0, null);

        g.drawImage(playerImage, WIDTH / 2 - PLANE_WIDTH/ 2, planeY, null);
        // 游戏标题
        g.setColor(Color.ORANGE);
        g.setFont(new Font("Arial", Font.BOLD, 64));
        g.drawString("FLAPPY PLANE", WIDTH/2 - 250, HEIGHT/3);
        
        // 游戏说明
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("How to Play:", WIDTH/2 - 120, HEIGHT/2 - 40);
        
        g.setFont(new Font("Arial", Font.PLAIN, 28));
        g.drawString("Press SPACE to make the plane jump", WIDTH/2 - 250, HEIGHT/2 + 30);
        g.drawString("Avoid hitting the pipes and boundaries", WIDTH/2 - 290, HEIGHT/2 + 80);
        
        // 开始提示
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("Press SPACE to Start Game", WIDTH/2 - 250, HEIGHT - 100);
        
        // // 绘制封面上的小鸟
        // g.setColor(Color.YELLOW);
        // g.fillRect(WIDTH/2 - PLANE_WIDTH/2, HEIGHT/3 + 50, PLANE_WIDTH, PLANE_HEIGHT);
        
        // 绘制封面上的管道
        g.setColor(Color.GRAY);
        g.fillRect(WIDTH/2 + 100, HEIGHT/3 - 100, 50, 200);
        g.fillRect(WIDTH/2 + 100, HEIGHT/3 + 150, 50, 200);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameStarted && !gameOver) {
            planeVelocity += GRAVITY;
            planeY += planeVelocity;

            // Generate pipes at random intervals
            pipeSpawnCounter++;
            if (pipeSpawnCounter >= nextSpawnDelay) {
                generatePipe();
                pipeSpawnCounter = 0;
            }

            movePipes();
            checkCollisions();
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_SPACE) {
            // 封面状态下按空格开始游戏
            if (showCover) {
                showCover = false;
                gameStarted = true;
                timer.start();
                loopbgm();
            }
            // 游戏进行中跳跃
            else if (gameStarted && !gameOver) {
                planeVelocity = -JUMP_STRENGTH;
            }
            // 游戏结束后按空格重新开始
            else if (gameOver) {
                
                resetGame();
                
            }
            // 游戏未开始状态按空格开始
            else if (!gameStarted) {
                gameStarted = true;
                timer.start();
                loopbgm();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public  void loadbgm(String filename) {
        try {
            File audioFile = new File(filename);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(audioStream);
        
        } catch (Exception e) {
            // You can leave this empty
            System.out.println("Not working");
        }
    }
    public void loopbgm(){
        if (backgroundClip != null) {
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stopbgm(){
        if (backgroundClip != null) {
            backgroundClip.stop();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        FlappyBird game = new FlappyBird();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
