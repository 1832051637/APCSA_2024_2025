package unused;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

// 游戏实体基类
abstract class Entity {
    protected int x, y;
    protected String symbol;
    protected int level;

    public Entity(int x, int y, String symbol, int level) {
        this.x = x;
        this.y = y;
        this.symbol = symbol;
        this.level = level;
    }

    public abstract void move();
}

// 玩家控制的生物
class Player extends Entity {
    public Player(int x, int y) {
        super(x, y, "蝌蚪", 1); // 初始为蝌蚪
    }

    public void evolve() {
        level++;
        switch (level) {
            case 2:
                symbol = "青";
                break; // 青蛙
            case 3:
                symbol = "龟";
                break; // 乌龟
            case 4:
                symbol = "龙";
                break; // 神龙
        }
    }

    @Override
    public void move() {
        // 由玩家控制移动
    }
}

class AIEntity extends Entity {
    private static final Random rand = new Random();
    private static final int WIDTH = 20;
    private static final int HEIGHT = 20;

    public AIEntity(int x, int y, int level) {
        super(x, y, getSymbolByLevel(level), level);
    }

    private static String getSymbolByLevel(int level) {
        return switch (level) {
            case 1 -> "虾";
            case 2 -> "蟹";
            case 3 -> "鲨";
            default -> "?";
        };
    }

    @Override
    public void move() {
        x += rand.nextInt(3) - 1;
        y += rand.nextInt(3) - 1;
        x = Math.max(0, Math.min(x, WIDTH - 1));
        y = Math.max(0, Math.min(y, HEIGHT - 1));
    }
}

// 游戏主逻辑
public class SummonDragonGame {
    private static final int WIDTH = 20;
    private static final int HEIGHT = 20;
    private Player player;
    private List<Entity> entities = new CopyOnWriteArrayList<>();
    private boolean isRunning = true;
    private Scanner scanner = new Scanner(System.in);

    public void initialize() {
        player = new Player(WIDTH / 2, HEIGHT / 2);
        entities.add(player);
        spawnInitialEntities();
    }

    private void spawnInitialEntities() {
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            entities.add(new AIEntity(
                    rand.nextInt(WIDTH),
                    rand.nextInt(HEIGHT),
                    rand.nextInt(3) + 1 // 1-3级
            ));
        }
    }

    private void processInput() {
        try { // 添加异常处理
            if (System.in.available() > 0) {
                char input = scanner.next().charAt(0);
                switch (input) {
                    case 'w':
                        player.y--;
                        break;
                    case 's':
                        player.y++;
                        break;
                    case 'a':
                        player.x--;
                        break;
                    case 'd':
                        player.x++;
                        break;
                    case 'q':
                        isRunning = false;
                        break;
                }
                player.x = Math.max(0, Math.min(player.x, WIDTH - 1));
                player.y = Math.max(0, Math.min(player.y, HEIGHT - 1));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateGame() {
        // 移动所有AI实体
        entities.forEach(e -> {
            if (e instanceof AIEntity)
                e.move();
        });

        // 碰撞检测
        checkCollisions();
    }

    private void checkCollisions() {
        Iterator<Entity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            Entity e = iterator.next();
            if (e == player)
                continue;

            if (e.x == player.x && e.y == player.y) {
                if (player.level >= e.level) {
                    System.out.println("吞噬了" + e.symbol + "!");
                    player.evolve();
                    iterator.remove();
                    spawnNewEntity();
                } else {
                    System.out.println("被" + e.symbol + "击败! 游戏结束");
                    isRunning = false;
                }
            }
        }
    }

    private void spawnNewEntity() {
        Random rand = new Random();
        entities.add(new AIEntity(
                rand.nextInt(WIDTH),
                rand.nextInt(HEIGHT),
                rand.nextInt(3) + 1));
    }

    private void render() {
        System.out.print("\033[H\033[2J"); // 清屏
        System.out.flush();

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                boolean hasEntity = false;
                for (Entity e : entities) {
                    if (e.x == x && e.y == y) {
                        System.out.print(e.symbol + " ");
                        hasEntity = true;
                        break;
                    }
                }
                if (!hasEntity)
                    System.out.print(". ");
            }
            System.out.println();
        }
        System.out.println("当前等级: " + player.symbol +
                " (WASD移动，Q退出)");
    }

    public void runGameLoop() throws InterruptedException {
        while (isRunning) {
            processInput();
            updateGame();
            render();
            TimeUnit.MILLISECONDS.sleep(300);
        }
    }

    public static void main(String[] args) throws Exception {
        SummonDragonGame game = new SummonDragonGame();
        game.initialize();
        game.runGameLoop();
    }
}