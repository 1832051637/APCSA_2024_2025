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

public class SpaceWavesDemo extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private int playerY = 350;
    private int velocityY = 10;
    private boolean up = false;
    private boolean gameOver = false;
    private boolean levelCompleteShown = false;
    private boolean showStartPage = true;

    private BufferedImage background;
    private BufferedImage player;
    private BufferedImage box;
    private BufferedImage triangle;
    private BufferedImage cricle;
    private BufferedImage GameoverPage;
    private BufferedImage GamestartPage;
    private BufferedImage LevelcompletePage;
    private BufferedImage bulletImage;

    // 拖尾特效相关变量
    private final ArrayList<Point> trailPositions = new ArrayList<>();
    private final ArrayList<Float> trailAlphas = new ArrayList<>();
    private final int TRAIL_LENGTH = 100;
    private final float ALPHA_DECREMENT;

    private int basicSpeed = 4;
    private int backgroundLength;
    private double gearAngle = 0;
    private int bgX = 0;
    private int playerX = 100;
    private final int playerWidth = 64;
    private final int playerHeight = 64;
    private boolean backgroundStarted = false;
    private final int minY = 275;
    private final int maxY = 649;

    private final ArrayList<Point> obstaclePositions = new ArrayList<>();
    private final ArrayList<BufferedImage> obstacleImages = new ArrayList<>();
    private final ArrayList<Point> bulletPositions = new ArrayList<>();
    private final Random rand = new Random();
    private final int LEVEL_END_X = 15000;

    private int speedBoost = 0;
    private boolean paused = false;
    private long lastFireTime = 0;
    private final long fireCooldown = 300;
    // 分数相关变量
    private int score = 0;
    private Font scoreFont;

    public SpaceWavesDemo() {
        ALPHA_DECREMENT = (1f / TRAIL_LENGTH) * 2f;
        JFrame frame = new JFrame("Space Waves - Java Demo");
        frame.setSize(2560, 1024);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.setResizable(false);

        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addKeyListener(this);

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                requestFocusInWindow();
            }
        });

        frame.setVisible(true);
        requestFocusInWindow();

        try {
            background = ImageIO.read(new File("images/background.png"));
            backgroundLength = background.getWidth();
            player = ImageIO.read(new File("images/player.png"));
            box = ImageIO.read(new File("images/box.png"));
            triangle = ImageIO.read(new File("images/triangle.png"));
            cricle = ImageIO.read(new File("images/cricle.png"));
            GameoverPage = ImageIO.read(new File("images/Gameover.png"));
            GamestartPage = ImageIO.read(new File("images/Gamestart.png"));
            LevelcompletePage = ImageIO.read(new File("images/LevelComplete.png"));
            bulletImage = ImageIO.read(new File("images/bullet.png"));

            // 加载自定义字体
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File("font/Lalezar.ttf"));
            scoreFont = customFont.deriveFont(Font.PLAIN, 60f); // 设置字体大小为 48，可自行调整
        } catch (Exception e) {
            e.printStackTrace();
        }

        generateObstacles(2560, backgroundLength * 1);
        timer = new Timer(20, this);
        timer.start();
    }

    private void generateObstacles(int startX, int length) {
        int currentX = startX;
        while (currentX < startX + length) {
            int baseGap = 200;
            int variableGap = 300 - (int) (currentX * 0.02);
            variableGap = Math.max(100, variableGap);
            int gap = baseGap + rand.nextInt(variableGap);

            int xTop = currentX + rand.nextInt(50);
            obstaclePositions.add(new Point(xTop, 275));
            obstacleImages.add(rand.nextBoolean() ? box : triangle);

            int xBottom = currentX + 80 + rand.nextInt(50);
            obstaclePositions.add(new Point(xBottom, 649));
            obstacleImages.add(rand.nextBoolean() ? box : triangle);

            if (rand.nextDouble() < 0.6) {
                int circleY = 402 + rand.nextInt(100);
                obstaclePositions.add(new Point(currentX + 150, circleY));
                obstacleImages.add(cricle);
            }

            currentX += gap;
        }
    }

    // 方法：fireBullet()
    private void fireBullet() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFireTime < fireCooldown)
            return;
        lastFireTime = currentTime;

        playSound("sound/Shoot.wav");

        bulletPositions.add(new Point(playerX + playerWidth, playerY + playerHeight / 2 - bulletImage.getHeight() / 2));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.BLACK);

        if (showStartPage && GamestartPage != null) {
            g.drawImage(GamestartPage, 0, 0, getWidth(), getHeight(), null);
            return;
        }

        if (gameOver && GameoverPage != null) {
            g.drawImage(GameoverPage, 0, 0, getWidth(), getHeight(), null);
            g.setFont(scoreFont);
            g.setColor(Color.WHITE);
            g.drawString("" + score, getWidth() / 2 - 22, getHeight() / 2 + 20); // 坐标可根据你的窗口布局调整
            return;
        }

        if (levelCompleteShown && LevelcompletePage != null) {
            g.drawImage(LevelcompletePage, 0, 0, getWidth(), getHeight(), null);
            return;
        }

        if (background != null) {
            g.drawImage(background, bgX, 0, null);
            g.drawImage(background, bgX + background.getWidth(), 0, null);
        }

        for (Point bullet : bulletPositions) {
            g.drawImage(bulletImage, bullet.x, bullet.y, null);
        }

        for (int i = 0; i < obstaclePositions.size(); i++) {
            Point p = obstaclePositions.get(i);
            BufferedImage img = obstacleImages.get(i);

            if (img == cricle) {
                Graphics2D g2d = (Graphics2D) g.create();
                int cx = p.x + bgX + img.getWidth() / 2;
                int cy = p.y + img.getHeight() / 2;
                g2d.translate(cx, cy);
                g2d.rotate(Math.toRadians(gearAngle));
                g2d.drawImage(img, -img.getWidth() / 2, -img.getHeight() / 2, null);
                g2d.dispose();
            } else if (p.y == 649) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.translate(p.x + bgX + img.getWidth(), p.y + img.getHeight() - 25);
                g2d.rotate(Math.toRadians(180));
                g2d.drawImage(img, 0, 0, null);
                g2d.dispose();
            } else {
                g.drawImage(img, p.x + bgX, p.y, null);
            }
        }

        // 绘制拖尾
        if (player != null && !showStartPage && !gameOver && !levelCompleteShown) {
            for (int i = 0; i < trailPositions.size(); i++) {
                Point pos = trailPositions.get(i);
                float alpha = trailAlphas.get(i);
                if (alpha <= 0.1f)
                    continue;

                Graphics2D g2d = (Graphics2D) g.create();
                AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
                g2d.setComposite(ac);
                g2d.drawImage(player, pos.x, pos.y, playerWidth, playerHeight, null);
                g2d.dispose();
            }
        }

        // 绘制玩家
        if (player != null) {
            g.drawImage(player, playerX, playerY, playerWidth, playerHeight, null);
        }
        // 绘制分数
        if (scoreFont != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setFont(scoreFont);
            g2d.setColor(Color.WHITE);
            g2d.drawString("Score: " + score, getWidth() / 2 - 100, 150); // 坐标可根据你的窗口布局调整
        }
    }

    public void update_trail_position(ArrayList<Point> trail_list) {
        for (int i = 0; i < trail_list.size(); i++) {
            trail_list.get(i).x -= basicSpeed + speedBoost;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (paused || gameOver || levelCompleteShown || showStartPage)
            return;

        // 更新拖尾数据
        if (backgroundStarted) {
            trailPositions.add(0, new Point(playerX, playerY));
            update_trail_position(trailPositions);
        } else
            trailPositions.add(0, new Point(playerX, playerY));

        trailAlphas.add(0, 1.0f);

        if (trailPositions.size() > TRAIL_LENGTH) {
            trailPositions.remove(trailPositions.size() - 1);
            trailAlphas.remove(trailAlphas.size() - 1);
        }

        for (int i = 0; i < trailAlphas.size(); i++) {
            trailAlphas.set(i, Math.max(0, trailAlphas.get(i) - ALPHA_DECREMENT));
        }

        for (int i = bulletPositions.size() - 1; i >= 0; i--) {
            Point bullet = bulletPositions.get(i);
            bullet.x += 15;
            if (bullet.x > getWidth()) {
                bulletPositions.remove(i);
                continue;
            }

            Rectangle bulletRect = new Rectangle(bullet.x, bullet.y, bulletImage.getWidth(), bulletImage.getHeight());
            if (-bgX > backgroundLength * 0.7) {
                generateObstacles((int) (-bgX + backgroundLength * 0.7), backgroundLength);
            }

            for (int j = obstaclePositions.size() - 1; j >= 0; j--) {
                Point obs = obstaclePositions.get(j);
                BufferedImage obsImg = obstacleImages.get(j);
                Rectangle obsRect = new Rectangle(obs.x + bgX, obs.y, obsImg.getWidth(), obsImg.getHeight());

                if (bulletRect.intersects(obsRect)) {
                    if (checkPixelCollision(bulletRect, obsRect, bulletImage, obsImg)) {
                        playSound("sound/collision.wav"); // 播放子弹碰撞音效
                        if (obsImg == cricle) {
                            bulletPositions.remove(i);
                            obstaclePositions.remove(j);
                            obstacleImages.remove(j);
                            score++; // 分数加1
                        }
                        break;
                    }
                }
            }
        }

        for (int i = obstaclePositions.size() - 1; i >= 0; i--) {
            if (obstaclePositions.get(i).x + bgX < -100) {
                obstaclePositions.remove(i);
                obstacleImages.remove(i);
            }
        }

        if (!gameOver) {
            checkCollision();

            velocityY = up ? -10 : 10;
            playerY += velocityY;
            limitPlayerBounds();

            if (!backgroundStarted) {
                if (playerX < getWidth() / 2 - playerWidth / 2) {
                    playerX += basicSpeed + speedBoost;
                } else {
                    backgroundStarted = true;
                    playerX = getWidth() / 2 - playerWidth / 2;
                }
            }

            if (backgroundStarted) {
                bgX -= basicSpeed + speedBoost;

                if (!levelCompleteShown && -bgX + playerX >= LEVEL_END_X) {
                    levelCompleteShown = true;
                    playSound("sounds/victory.wav"); // 播放胜利音效
                    obstaclePositions.clear();
                    obstacleImages.clear();
                    bulletPositions.clear();
                    repaint();
                    return;
                }

                if (bgX + background.getWidth() <= 0) {
                    bgX = 0;
                }
            }
        }

        gearAngle = (gearAngle + 5) % 360;
        repaint();
    }

    private boolean checkPixelCollision(Rectangle rect1, Rectangle rect2, BufferedImage img1, BufferedImage img2) {
        int x1 = Math.max(rect1.x, rect2.x);
        int y1 = Math.max(rect1.y, rect2.y);
        int x2 = Math.min(rect1.x + rect1.width, rect2.x + rect2.width);
        int y2 = Math.min(rect1.y + rect1.height, rect2.y + rect2.height);

        for (int y = y1; y < y2; y++) {
            for (int x = x1; x < x2; x++) {
                int img1X = x - rect1.x;
                int img1Y = y - rect1.y;
                int img2X = x - rect2.x;
                int img2Y = y - rect2.y;

                if (img1X >= 0 && img1X < img1.getWidth() &&
                        img1Y >= 0 && img1Y < img1.getHeight() &&
                        img2X >= 0 && img2X < img2.getWidth() &&
                        img2Y >= 0 && img2Y < img2.getHeight()) {
                    int alpha1 = (img1.getRGB(img1X, img1Y) >> 24) & 0xff;
                    int alpha2 = (img2.getRGB(img2X, img2Y) >> 24) & 0xff;

                    if (alpha1 > 0 && alpha2 > 0)
                        return true;
                }
            }
        }
        return false;
    }

    private void checkCollision() {
        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        for (int i = 0; i < obstaclePositions.size(); i++) {
            Point p = obstaclePositions.get(i);
            BufferedImage obsImg = obstacleImages.get(i);
            int obsX = p.x + bgX;
            int obsY = p.y;
            Rectangle obsRect = new Rectangle(obsX, obsY, obsImg.getWidth(), obsImg.getHeight());

            if (!playerRect.intersects(obsRect))
                continue;

            int intersectX1 = Math.max(playerRect.x, obsRect.x);
            int intersectY1 = Math.max(playerRect.y, obsRect.y);
            int intersectX2 = Math.min(playerRect.x + playerRect.width, obsRect.x + obsRect.width);
            int intersectY2 = Math.min(playerRect.y + playerRect.height, obsRect.y + obsRect.height);

            for (int y = intersectY1; y < intersectY2; y++) {
                for (int x = intersectX1; x < intersectX2; x++) {
                    int obsXIndex = x - obsX;
                    int obsYIndex = y - obsY;
                    int playerXIndex = x - playerX;
                    int playerYIndex = y - playerY;

                    if (obsXIndex >= 0 && obsXIndex < obsImg.getWidth() &&
                            obsYIndex >= 0 && obsYIndex < obsImg.getHeight() &&
                            playerXIndex >= 0 && playerXIndex < player.getWidth() &&
                            playerYIndex >= 0 && playerYIndex < player.getHeight()) {

                        int obsAlpha = (obsImg.getRGB(obsXIndex, obsYIndex) >> 24) & 0xff;
                        int playerAlpha = (player.getRGB(playerXIndex, playerYIndex) >> 24) & 0xff;

                        if (obsAlpha > 0 && playerAlpha > 0) {
                            gameOver = true;
                            playSound("sound/Death.wav");
                            player = null;
                            obstaclePositions.clear();
                            obstacleImages.clear();
                            bulletPositions.clear();

                            bgX = 0;
                            return;
                        }
                    }
                }
            }
        }
    }

    private void playSound(String soundFilePath) {
        try {
            File soundFile = new File(soundFilePath);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void limitPlayerBounds() {
        if (playerY < minY)
            playerY = minY;
        if (playerY > maxY)
            playerY = maxY;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (gameOver || levelCompleteShown) {
            if (code == KeyEvent.VK_R) {
                restartGame();
            } else if (code == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }
            return;
        }

        if (showStartPage) {
            if (code == KeyEvent.VK_SPACE) {
                showStartPage = false;
            }
            return;
        }

        switch (code) {
            case KeyEvent.VK_SPACE -> up = true;
            case KeyEvent.VK_F -> {
                if (!paused)
                    fireBullet();
            }
            case KeyEvent.VK_0 -> speedBoost = 0;
            case KeyEvent.VK_1 -> speedBoost = 5;
            case KeyEvent.VK_2 -> speedBoost = 10;
            case KeyEvent.VK_3 -> speedBoost = 15;
            case KeyEvent.VK_4 -> speedBoost = 20;
            case KeyEvent.VK_T -> speedBoost = 50;
            case KeyEvent.VK_P -> paused = !paused;
            case KeyEvent.VK_ESCAPE -> System.exit(0);
        }
    }

    private void restartGame() {
        gameOver = false;
        paused = false;
        backgroundStarted = false;
        bgX = 0;
        levelCompleteShown = false;
        playerX = 100;
        playerY = 350;
        speedBoost = 0;
        score = 0; // 重新开始时分数归零
        showStartPage = false;
        trailPositions.clear(); // 清空拖尾数据
        trailAlphas.clear();
        try {
            player = ImageIO.read(new File("images/player.png"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        obstaclePositions.clear();
        obstacleImages.clear();
        bulletPositions.clear();
        generateObstacles(2560, LEVEL_END_X);
        requestFocusInWindow();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE)
            up = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        // System.setProperty("sun.java2d.opengl", "true");
        new Thread(() -> {
            try {
                File bgmFile = new File("sound/BackgroundSound.wav");
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(bgmFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.loop(Clip.LOOP_CONTINUOUSLY); // 🔁 循环播放
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        System.setProperty("sun.java2d.opengl", "true");
        SwingUtilities.invokeLater(() -> {
            SpaceWavesDemo game = new SpaceWavesDemo();
            game.requestFocusInWindow();
        });
    }
}