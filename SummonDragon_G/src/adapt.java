import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

// 游戏实体基类
abstract class GameEntity {
    protected int level;
    protected int health;
    protected int attack;
    protected boolean isAlive;

    public GameEntity(int level, int health, int attack) {
        this.level = level;
        this.health = health;
        this.attack = attack;
        this.isAlive = true;
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            isAlive = false;
        }
    }

    // Getter方法
    public int getLevel() {
        return level;
    }

    public int getHealth() {
        return health;
    }

    public int getAttack() {
        return attack;
    }

    public boolean isAlive() {
        return isAlive;
    }
}

// 玩家类
class Player extends GameEntity {
    private String playerId;
    private int score;
    private double survivalTime;
    private int combineCount;
    private boolean berserkState;
    private int berserkCounter;
    private Map<String, Talent> talents;

    public Player(int level, int health, int attack, String playerId) {
        super(level, health, attack);
        this.playerId = playerId;
        this.score = 0;
        this.survivalTime = 0;
        this.combineCount = 0;
        this.berserkState = false;
        this.berserkCounter = 0;
        this.talents = new HashMap<>();
    }

    public void updateSurvivalTime(double deltaTime) {
        survivalTime += deltaTime;
    }

    public void increaseCombineCount() {
        combineCount++;
        // 连续吞噬5次触发狂暴状态
        if (combineCount % 5 == 0) {
            enterBerserkState();
        }
    }

    public void enterBerserkState() {
        berserkState = true;
        berserkCounter = 10; // 狂暴状态持续10秒
        System.out.println("玩家 " + playerId + " 进入狂暴状态！");
    }

    public void updateBerserkState(double deltaTime) {
        if (berserkState) {
            berserkCounter -= deltaTime;
            if (berserkCounter <= 0) {
                berserkState = false;
                System.out.println("玩家 " + playerId + " 狂暴状态结束！");
            }
        }
    }

    // 解锁天赋
    public void unlockTalent(Talent talent) {
        talents.put(talent.getName(), talent);
        System.out.println("玩家 " + playerId + " 解锁天赋: " + talent.getName());
    }

    // 应用天赋效果
    public int getModifiedAttack() {
        int baseAttack = attack;
        for (Talent talent : talents.values()) {
            if (talent instanceof AttackTalent) {
                baseAttack = ((AttackTalent) talent).modifyAttack(baseAttack, level);
            }
        }
        return baseAttack;
    }

    public int getModifiedInvincibilityTime() {
        double baseTime = 2.0; // 基础无敌时间2秒
        for (Talent talent : talents.values()) {
            if (talent instanceof SurvivalTalent) {
                baseTime = ((SurvivalTalent) talent).modifyInvincibilityTime(baseTime, level);
            }
        }
        return (int) Math.ceil(baseTime);
    }

    // Getter方法
    public String getPlayerId() {
        return playerId;
    }

    public int getScore() {
        return score;
    }

    public double getSurvivalTime() {
        return survivalTime;
    }

    public int getCombineCount() {
        return combineCount;
    }

    public boolean isBerserkState() {
        return berserkState;
    }
}

// 敌人类
class Enemy extends GameEntity {
    private double spawnProbability;
    private boolean isElite;

    public Enemy(int level, int health, int attack, double spawnProbability, boolean isElite) {
        super(level, health, attack);
        this.spawnProbability = spawnProbability;
        this.isElite = isElite;
    }

    // 判断是否可以被玩家吞噬
    public boolean canBeEatenBy(Player player) {
        if (player.isBerserkState()) {
            return player.getLevel() + 1 >= level; // 狂暴状态可吞噬高一级敌人
        }
        return player.getLevel() >= level; // 正常状态只能吞噬同级或更低
    }

    // Getter方法
    public double getSpawnProbability() {
        return spawnProbability;
    }

    public boolean isElite() {
        return isElite;
    }
}

// 天赋接口
interface Talent {
    String getName();
}

// 生存天赋
class SurvivalTalent implements Talent {
    private String name;
    private double talentCoefficient;

    public SurvivalTalent(String name, double talentCoefficient) {
        this.name = name;
        this.talentCoefficient = talentCoefficient;
    }

    public double modifyInvincibilityTime(double baseTime, int level) {
        return baseTime + talentCoefficient * level;
    }

    @Override
    public String getName() {
        return name;
    }
}

// 攻击天赋
class AttackTalent implements Talent {
    private String name;
    private double successRate; // 反杀成功率

    public AttackTalent(String name, double successRate) {
        this.name = name;
        this.successRate = successRate;
    }

    public int modifyAttack(int baseAttack, int level) {
        return baseAttack; // 基础攻击不变，通过反杀逻辑实现效果
    }

    // 低等级生物反杀高一级敌人的判定
    public boolean canKillHigherLevel() {
        return Math.random() < successRate;
    }

    @Override
    public String getName() {
        return name;
    }
}

// 动态难度调整系统
class DynamicDifficultySystem {
    private Random random;
    private Map<Integer, Double> enemySpawnWeights; // 不同等级敌人的生成权重
    private Player player;

    public DynamicDifficultySystem(Player player) {
        this.random = new Random();
        this.player = player;
        this.enemySpawnWeights = new HashMap<>();

        // 初始化权重
        for (int i = 1; i <= 10; i++) {
            enemySpawnWeights.put(i, 1.0);
        }
    }

    // 根据玩家表现调整难度
    public void adjustDifficulty() {
        // 连续失败时降低高等级敌人生成概率
        if (player.getSurvivalTime() < 10 && player.getCombineCount() < 3) {
            for (int i = player.getLevel() + 1; i <= 10; i++) {
                enemySpawnWeights.put(i, enemySpawnWeights.get(i) * 0.7); // 降低30%概率
            }
        }
        // 快速升级时增加敌人干扰
        else if (player.getScore() > 1000 && player.getLevel() > 5) {
            for (int i = player.getLevel() - 1; i <= player.getLevel() + 1; i++) {
                if (i >= 1 && i <= 10) {
                    enemySpawnWeights.put(i, enemySpawnWeights.get(i) * 1.3); // 增加30%概率
                }
            }
        }
    }

    // 生成敌人
    public Enemy spawnEnemy() {
        adjustDifficulty();

        // 计算总权重
        double totalWeight = 0;
        for (double weight : enemySpawnWeights.values()) {
            totalWeight += weight;
        }

        // 加权随机选择敌人等级
        double randomValue = random.nextDouble() * totalWeight;
        double cumulativeWeight = 0;
        int selectedLevel = 1;

        for (Map.Entry<Integer, Double> entry : enemySpawnWeights.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (randomValue <= cumulativeWeight) {
                selectedLevel = entry.getKey();
                break;
            }
        }

        // 精英怪生成概率随玩家等级增加
        boolean isElite = random.nextDouble() < 0.1 * player.getLevel() && player.getLevel() <= 5;

        return new Enemy(
                selectedLevel,
                10 * selectedLevel + (isElite ? 50 : 0),
                5 * selectedLevel + (isElite ? 20 : 0),
                enemySpawnWeights.get(selectedLevel) / totalWeight,
                isElite);
    }
}

// 随机事件系统
class RandomEventSystem {
    private Random random;
    private Player player;

    public RandomEventSystem(Player player) {
        this.random = new Random();
        this.player = player;
    }

    // 生成随机事件
    public GameEvent generateEvent() {
        // 事件触发概率与玩家等级负相关
        double triggerProbability = 0.2 - 0.01 * player.getLevel();
        if (triggerProbability < 0.05) {
            triggerProbability = 0.05; // 最低概率5%
        }

        if (random.nextDouble() < triggerProbability) {
            int eventType = random.nextInt(3);
            switch (eventType) {
                case 0:
                    return new SpeedBoostEvent(5.0); // 5秒加速
                case 1:
                    return new FreezeEvent(3.0); // 3秒冻结
                case 2:
                    return new HealthBoostEvent(50); // 恢复50点生命
                default:
                    return null;
            }
        }
        return null;
    }
}

// 游戏事件抽象类
abstract class GameEvent {
    private double duration;
    private long startTime;

    public GameEvent(double duration) {
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }

    public boolean isActive() {
        return (System.currentTimeMillis() - startTime) / 1000.0 < duration;
    }

    public abstract void applyEffect(Player player, List<Enemy> enemies);

    public abstract String getEventName();
}
