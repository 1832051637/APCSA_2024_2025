
// 这是一个简单的Java Swing游戏，模拟了一个神秘池塘的生态系统。
// 游戏中有不同等级的生物，玩家可以通过移动和合并生物来升级。
// 游戏的目标是召唤神龙，玩家需要在池塘中生存并与其他生物互动。
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.geom.AffineTransform;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.List;
import javax.swing.Timer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.awt.image.BufferedImage;
import java.net.URL;

public class DragonPondGame extends JPanel implements Runnable {
    // 游戏配置
    private static final String[] LEVELS = { "蝌蚪", "青蛙", "乌龟", "鱼", "鲨鱼", "鲸鱼", "神龙" };
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int TILE_SIZE = 64;
    private static final int SPAWN_INTERVAL = 3000;
    private static final Color[] LEVEL_COLORS = {
            new Color(102, 205, 170), // 蝌蚪-蓝绿
            new Color(50, 205, 50), // 青蛙-绿
            new Color(139, 69, 19), // 乌龟-棕色
            new Color(30, 144, 255), // 鱼-蓝色
            new Color(178, 34, 34), // 鲨鱼-深红
            new Color(0, 0, 128), // 鲸鱼-海军蓝
            new Color(147, 112, 219) // 神龙-紫色
    };

    private int viewOffsetX = WIDTH / 2;
    private int viewOffsetY = HEIGHT / 2;

    private Map<Integer, Integer> creatureCountMap = new HashMap<>();

    // 游戏实体
    private Player player;
    private List<Creature> creatures = new CopyOnWriteArrayList<>();
    private Map<Integer, MovementStrategy> movementStrategies = new HashMap<>();
    private Random random = new Random();
    private int viewRadius = 200;
    private int playerLevel = 0;

    // 添加线程池
    private final ExecutorService mergeExecutor = Executors.newFixedThreadPool(2);

    private static final int[] VALUE_CACHE = { 1, 3, 9, 27, 81, 243, 729 };

    public DragonPondGame() {
        initMovementStrategies();
        initPlayer();
        startGameLoop();
        spawnInitialCreatures();
        System.out.println("测试图片加载：");
        for (int i = 0; i < LEVELS.length; i++) {
            Image img = getImageForLevel(i);
            System.out.println("Lv" + i + ": " +
                    (img != null ? "加载成功" : "加载失败"));
        }
    }

    private void initMovementStrategies() {
        movementStrategies.put(0, new TadpoleMovement()); // 蝌蚪随机游动
        movementStrategies.put(1, new FrogMovement()); // 青蛙跳跃移动
        movementStrategies.put(2, new TurtleMovement()); // 乌龟直线慢速
        movementStrategies.put(3, new FishMovement()); // 鱼类正弦曲线
        movementStrategies.put(4, new SharkMovement()); // 鲨鱼追击玩家
        movementStrategies.put(5, new WhaleMovement()); // 鲸鱼缓慢移动
        movementStrategies.put(6, new DragonMovement()); // 蛟龙盘旋移动
    }

    private void spawnInitialCreatures() {
        for (int i = 0; i < 8; i++) {
            int x, y;
            do {
                x = random.nextInt(WIDTH);
                y = random.nextInt(HEIGHT);
            } while (distance(new GameEntity() {
                {
                    setPosition(x, y);
                }
            }, player) < viewRadius); // 确保初始生物在视野外
            creatures.add(new Creature(x, y, 0, 1));
        }
    }

    private void initPlayer() {
        player = new Player(WIDTH / 2, HEIGHT / 2, 0);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_R) { // R键生成测试生物
                    creatures.add(new Creature(player.x + 50, player.y + 50, playerLevel, 2));
                }
                handlePlayerMovement(e.getKeyCode(), true);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handlePlayerMovement(e.getKeyCode(), false);
            }
        });
        setFocusable(true);

        // 在DragonPondGame构造函数中添加
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point mousePoint = e.getPoint();
                // 计算玩家朝向角度
                double angle = Math.atan2(mousePoint.y - HEIGHT / 2, mousePoint.x - WIDTH / 2);
                player.direction = (int) Math.toDegrees(angle);
            }
        });

        // 添加鼠标点击监听
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                player.setTarget(e.getX(), e.getY());
            }
        });
    }

    private void handlePlayerMovement(int keyCode, boolean isPressed) {
        switch (keyCode) {
            case KeyEvent.VK_W -> player.movingUp = isPressed;
            case KeyEvent.VK_S -> player.movingDown = isPressed;
            case KeyEvent.VK_A -> player.movingLeft = isPressed;
            case KeyEvent.VK_D -> player.movingRight = isPressed;
        }
    }

    private void startGameLoop() {
        new Thread(this).start();
        new java.util.Timer().schedule(new CreatureSpawnTask(), SPAWN_INTERVAL, SPAWN_INTERVAL);
    }

    private class CreatureSpawnTask extends TimerTask {
        @Override
        public void run() {
            int level = Math.min(playerLevel + 1, LEVELS.length - 1);
            int attempts = 0;
            int speed;
            while (attempts++ < 10) {
                int x = random.nextInt(WIDTH);
                int y = random.nextInt(HEIGHT);
                // 确保生成位置在玩家视野外
                if (distance(new GameEntity() {
                    {
                        setPosition(x, y);
                    }
                }, player) > viewRadius + 100) {
                    if (level >= 2) {
                        speed = 2 / level;
                    } else {
                        speed = 1;
                    }
                    creatures.add(new Creature(x, y, level, speed));
                    break;
                }
            }
        }
    }

    private double distance(int x1, int y1, int x2, int y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }

    private void constrainToArea(GameEntity entity) {
        entity.x = Math.max(TILE_SIZE / 2, Math.min(WIDTH - TILE_SIZE / 2, entity.x));
        entity.y = Math.max(TILE_SIZE / 2, Math.min(HEIGHT - TILE_SIZE / 2, entity.y));
    }

    // 各生物运动策略实现
    class TadpoleMovement implements MovementStrategy {
        public void move(Creature c) {
            if (random.nextDouble() < 0.05) {
                c.direction += random.nextInt(180) - 90;
            }
            c.x += Math.cos(Math.toRadians(c.direction)) * c.speed;
            c.y += Math.sin(Math.toRadians(c.direction)) * c.speed;
        }
    }

    class FrogMovement implements MovementStrategy {
        private double jumpTimer = 0;

        public void move(Creature c) {
            jumpTimer += 0.1;
            if (jumpTimer > 2 * Math.PI)
                jumpTimer -= 2 * Math.PI;

            c.x += Math.cos(Math.toRadians(c.direction)) * c.speed * Math.abs(Math.sin(jumpTimer));
            c.y += Math.sin(Math.toRadians(c.direction)) * c.speed * Math.abs(Math.sin(jumpTimer));
        }
    }

    class SharkMovement implements MovementStrategy {
        public void move(Creature c) {
            double angle = Math.atan2(player.y - c.y, player.x - c.x);
            c.x += Math.cos(angle) * c.speed;
            c.y += Math.sin(angle) * c.speed;
        }
    }

    class TurtleMovement implements MovementStrategy {
        public void move(Creature c) {
            c.x += Math.cos(Math.toRadians(c.direction)) * c.speed;
            if (random.nextDouble() < 0.01)
                c.direction = random.nextInt(360);
        }
    }

    class FishMovement implements MovementStrategy {
        private double angle = 0;

        public void move(Creature c) {
            angle += 0.1;
            c.x += Math.cos(Math.toRadians(c.direction)) * c.speed;
            c.y += Math.sin(Math.toRadians(c.direction)) * c.speed + Math.sin(angle) * 3;
        }
    }

    class WhaleMovement implements MovementStrategy {
        public void move(Creature c) {
            c.x += Math.cos(Math.toRadians(c.direction)) * c.speed;
            if (random.nextDouble() < 0.005)
                c.direction = random.nextInt(360);
        }
    }

    class DragonMovement implements MovementStrategy {
        private double spiralAngle = 0;

        public void move(Creature c) {
            spiralAngle += 0.1;
            c.x += Math.cos(spiralAngle) * 3;
            c.y += Math.sin(spiralAngle) * 3;
            c.x += Math.cos(Math.toRadians(c.direction)) * c.speed;
            c.y += Math.sin(Math.toRadians(c.direction)) * c.speed;
        }
    }

    private void updateGame() {
        updatePlayer();
        updateCreatures();
        updateSpatialGrid();
        checkCollisions();
        checkMerge();
    }

    private void updatePlayer() {
        player.update();
        constrainToArea(player);
    }

    private void updateCreatureCount() {
        creatureCountMap.clear();
        creatures.stream()
                .filter(c -> distance(c, player) < 100) // 检测玩家周围100像素内的生物
                .filter(c -> c.level <= playerLevel) // 只统计低于等于玩家等级的
                .forEach(c -> creatureCountMap.merge(c.level, 1, Integer::sum));
    }

    private void updateCreatures() {
        for (Creature c : creatures) {
            if (c.level > playerLevel)
                c.chase(player);
            movementStrategies.get(c.level).move(c);
            constrainToArea(c);
            // if (c instanceof Particle)
            // ((Particle) c).update();
        }
    }

    private void checkCollisions() {
        creatures.stream()
                .filter(c -> distance(c, player) < TILE_SIZE) // 使用实际距离
                .filter(c -> c.level > playerLevel)
                .findFirst()
                .ifPresent(c -> gameOver());
    }

    private void createNewCreature(int newLevel) {
        // 在新位置生成高级生物
        IntStream.range(0, 2).forEach(i -> {
            creatures.add(new Creature(
                    player.x + random.nextInt(80) - 40,
                    player.y + random.nextInt(80) - 40,
                    newLevel,
                    newLevel * 2 + 1));
        });

        // 玩家升级条件
        if (newLevel > playerLevel) {
            playerLevel = newLevel;
            viewRadius += 30;
        }
    }

    private void checkMerge() {
        updateCreatureCount();

        creatureCountMap.forEach((level, count) -> {
            if (count >= 3 && level < LEVELS.length - 1) {
                performGroupMerge(level);
            }
        });
    }

    private void performGroupMerge(int baseLevel) {
        List<Creature> toRemove = creatures.stream()
                .filter(c -> c.level == baseLevel)
                .filter(c -> distance(c, player) < 100)
                .limit(3)
                .collect(Collectors.toList());

        if (toRemove.size() >= 3) {
            creatures.removeAll(toRemove);
            createNewCreature(baseLevel + 1);
            // showEvolutionAnimation(toRemove);
        }
    }

    // private void showEvolutionAnimation(List<Creature> merged) {
    // // 显示升级提示
    // JLabel tip = new JLabel("进化成 " + LEVELS[merged.get(0).level + 1] + "!");
    // tip.setForeground(Color.YELLOW);
    // tip.setFont(new Font("宋体", Font.BOLD, 24));
    // tip.setBounds(player.x - 50, player.y - 100, 200, 50);
    // add(tip);

    // javax.swing.Timer uiTimer = new javax.swing.Timer(1000, e -> {
    // this.remove(tip);
    // this.revalidate();
    // this.repaint();
    // ((javax.swing.Timer) e.getSource()).stop();// 停止计时器
    // });
    // uiTimer.start();
    // }

    // // 新增粒子效果类
    // class Particle extends Creature {
    // Color color;
    // int lifespan = 20;

    // Particle(int x, int y, Color color, int level) {
    // super(x, y, level, 5);
    // this.color = color;
    // }

    // void update() {
    // if (--lifespan <= 0)
    // creatures.remove(this);
    // }

    // }

    private void gameOver() {
        JOptionPane.showMessageDialog(this, "被高级生物捕获！游戏结束！");
        resetGame();
    }

    // 添加resetGame方法
    private void resetGame() {
        mergeExecutor.shutdownNow();
        playerLevel = 0;
        viewRadius = 200;
        creatures.clear();
        spawnInitialCreatures();
        player.x = WIDTH / 2;
        player.y = HEIGHT / 2;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 在左上角绘制收集进度
        final int[] yPos = { 30 };
        g2d.setColor(Color.BLACK);
        g2d.drawString("当前等级: " + LEVELS[playerLevel], 20, yPos[0]);
        // g2d.drawString("当前分数: " + player.score, 120, yPos[0]);

        creatureCountMap.forEach((level, count) -> {
            yPos[0] += 20;
            String text = LEVELS[level] + ": " + count + "/3";
            g2d.drawString(text, 20, yPos[0]);

            // // 绘制进度条
            // int width = (int) (100 * (count / 3.0));
            // g2d.setColor(LEVEL_COLORS[level]);
            // g2d.fillRect(120, yPos[0] - 12, width, 10);

            // 绘制可见区域（修改为基于视口）
            drawVisibleArea(g2d);

            // 绘制玩家
            drawRotatedCreature(g2d, player);
            creatures.stream()
                    .filter(c -> isInView(c))
                    .forEach(c -> drawCreature(g2d, c));

            // 绘制其他生物（应用偏移）
            creatures.stream()
                    .filter(c -> isInView(c))
                    .forEach(c -> {
                        int renderX = c.x - viewOffsetX;
                        int renderY = c.y - viewOffsetY;
                        drawCreature(g2d, new GameEntity() {
                            {
                                x = renderX;
                                y = renderY;
                                level = c.level;
                            }
                        });
                    });
        });
    }

    private void drawVisibleArea(Graphics2D g2d) {
        // 创建圆形视野
        Shape clip = new Ellipse2D.Float(
                player.x - viewRadius,
                player.y - viewRadius,
                viewRadius * 2,
                viewRadius * 2);
        g2d.setClip(clip);

        // 绘制池塘背景
        g2d.setColor(new Color(100, 200, 255));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        g2d.setClip(null); // 重置裁剪区域

        // 绘制迷雾效果 - view.png
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        g2d.setComposite(AlphaComposite.SrcOver);
    }

    private boolean isInView(GameEntity entity) {
        return distance(entity, player) < viewRadius;
    }

    private double distance(GameEntity a, GameEntity b) {
        return Math.hypot(a.x - b.x, a.y - b.y);
    }

    private void drawRotatedCreature(Graphics2D g2d, GameEntity entity) {
        AffineTransform old = g2d.getTransform();
        g2d.rotate(Math.toRadians(entity.direction), entity.x, entity.y);
        drawCreature(g2d, entity);
        g2d.setTransform(old);
    }

    private void drawCreature(Graphics2D g2d, GameEntity entity) {
        Image image = getImageForLevel(entity.level);

        // 绘制图像或颜色形状
        if (image != null) {
            g2d.drawImage(image, entity.x - TILE_SIZE / 2, entity.y - TILE_SIZE / 2, null);
        } else {
            // 根据等级绘制颜色形状
            g2d.setColor(LEVEL_COLORS[Math.min(entity.level, LEVEL_COLORS.length - 1)]);
            if (entity.level > playerLevel) {
                // 高级生物显示警告色
                g2d.setColor(Color.RED);
                g2d.fillRect(entity.x - 32, entity.y - 32, 64, 64);
            } else {
                // 基础形状+等级标识
                g2d.fillOval(entity.x - 24, entity.y - 24, 48, 48);
                g2d.setColor(Color.WHITE);
                g2d.drawString("Lv." + entity.level, entity.x - 15, entity.y + 8);
            }
        }
        // 添加玩家等级指示器
        if (entity == player) {
            g2d.setColor(Color.YELLOW);
            g2d.drawOval(entity.x - 34, entity.y - 34, 68, 68);
            // 添加玩家等级显示
            g2d.setColor(Color.WHITE);
            g2d.drawString("Lv." + playerLevel, entity.x - 15, entity.y + 40);
        }
    }

    private Image getImageForLevel(int level) {
        try {
            // 定义图片名称映射
            String[] imageFiles = {
                    "tadpole.png", // Lv0
                    "frog.png", // Lv1
                    "turtle.png", // Lv2
                    "fish.png", // Lv3
                    "shark.png", // Lv4
                    "whale.png", // Lv5
                    "dragon.png" // Lv6
            };

            // 安全获取资源路径
            String imageFolder = "resource/image/";
            int safeLevel = Math.min(level, imageFiles.length - 1);
            URL imgUrl = getClass().getResource(imageFolder + imageFiles[safeLevel]);

            if (imgUrl == null) {
                System.err.println("图片未找到: " + imageFiles[safeLevel]);
                return createFallbackImage(level); // 生成备用图形
            }

            // 高质量缩放
            Image original = new ImageIcon(imgUrl).getImage();
            return original;

        } catch (Exception e) {
            System.err.println("图片加载错误: " + e.getMessage());
            return createFallbackImage(level);
        }
    }

    private Image createFallbackImage(int level) {
        // 生成带等级数字的彩色圆形
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // 设置颜色
        Color[] colors = { Color.CYAN, Color.GREEN, Color.ORANGE,
                Color.BLUE, Color.RED, Color.MAGENTA, Color.YELLOW };
        g.setColor(colors[Math.min(level, colors.length - 1)]);
        g.fillOval(4, 4, 56, 56);

        // 绘制等级数字
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        String text = "" + level;
        int x = 32 - g.getFontMetrics().stringWidth(text) / 2;
        int y = 32 + 8;
        g.drawString(text, x, y);

        g.dispose();
        return img;
    }

    // 生物运动策略接口
    interface MovementStrategy {
        void move(Creature creature);
    }

    class Player extends GameEntity {
        boolean movingUp, movingDown, movingLeft, movingRight;
        private int targetX, targetY;
        private boolean hasTarget = false;

        void setTarget(int x, int y) {
            targetX = x + viewOffsetX;
            targetY = y + viewOffsetY;
            hasTarget = true;
        }

        Player(int x, int y, int level) {
            setPosition(x, y);
            this.level = level;
            this.speed = 3;
            this.score = 0;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        // 修改Player的update方法实现自动移动
        void update() {
            if (hasTarget) {
                double angle = Math.atan2(targetY - y, targetX - x);
                x += Math.cos(angle) * speed;
                y += Math.sin(angle) * speed;

                if (distance(this.x, this.y, targetX, targetY) < 5) {
                    hasTarget = false;
                }
            }
            // 根据键盘输入移动
            if (player.movingUp)
                player.y -= player.speed;
            if (player.movingDown)
                player.y += player.speed;
            if (player.movingLeft)
                player.x -= player.speed;
            if (player.movingRight)
                player.x += player.speed;

            x = Math.max(TILE_SIZE / 2, Math.min(WIDTH - TILE_SIZE / 2, x));
            y = Math.max(TILE_SIZE / 2, Math.min(HEIGHT - TILE_SIZE / 2, y));

            // 根据鼠标方向补充移动
            double radian = Math.toRadians(player.direction);
            x += Math.cos(radian) * player.speed * 0.5;
            y += Math.sin(radian) * player.speed * 0.5;
        }
    }

    class Creature extends GameEntity {
        Creature(int x, int y, int level, int speed) {
            this.x = x;
            this.y = y;
            this.level = level;
            this.speed = speed;
            this.direction = new Random().nextInt(360);
        }

        void chase(Player target) {
            double angle = Math.atan2(target.y - y, target.x - x);
            direction = (int) Math.toDegrees(angle);
            x += Math.cos(angle) * speed;
            y += Math.sin(angle) * speed;
        }
    }

    // 实体基类
    abstract class GameEntity {
        protected int x; // 改为protected访问权限
        protected int y;
        int level;
        int direction;
        int speed;
        int score;

        public void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        Rectangle getBounds() {
            return new Rectangle(x - TILE_SIZE / 4, y - TILE_SIZE / 4,
                    TILE_SIZE / 2, TILE_SIZE / 2);
        }
    }

    // 添加空间网格
    private Map<GridPos, List<Creature>> spatialGrid = new HashMap<>();

    class GridPos {
        int x, y;

        GridPos(int x, int y) {
            this.x = x / 100; // 每100像素为一个格子
            this.y = y / 100;
        }
    }

    // 更新时维护网格
    private void updateSpatialGrid() {
        spatialGrid.clear();
        for (Creature c : creatures) {
            GridPos pos = new GridPos(c.x - viewOffsetX, c.y - viewOffsetY);
            spatialGrid.computeIfAbsent(pos, k -> new ArrayList<>()).add(c);
        }
    }

    private long lastLogTime = System.currentTimeMillis();

    public void run() {
        while (true) {
            long frameStart = System.nanoTime();

            // 主逻辑更新
            updateGame();

            // 降低渲染频率到30FPS
            if (System.nanoTime() - lastLogTime > 33_000_000) {
                repaint();
                lastLogTime = System.nanoTime();
            }

            // 自适应帧率控制
            long frameTime = System.nanoTime() - frameStart;
            long sleepTime = Math.max(0, 16 - (frameTime / 1_000_000));
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                /* 处理异常 */ }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("神秘池塘");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(DragonPondGame.WIDTH, DragonPondGame.HEIGHT);
            frame.add(new DragonPondGame());
            frame.setVisible(true);
        });
    }
}