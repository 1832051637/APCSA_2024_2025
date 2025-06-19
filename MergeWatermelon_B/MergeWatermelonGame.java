import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

public class MergeWatermelonGame extends JPanel {
    // 重力值
    private static final double GRAVITY = 0.3;
    // 阻尼系数（越大阻力越小）
    private static final double DAMPING = 0.98;
    // 弹跳能量损失系数
    private static final double BOUNCE_DAMPING = 0.6;
    // 窗口宽度和高度
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 500;
    // 游戏状态
    private boolean gameOver = false;
    private boolean gameWin = false;
    private boolean isPaused = false;
    // 物体列表
    private List<GameObject> objectList = new ArrayList<>();
    // 当前控制的物体
    private GameObject currentObject = null;
    // 分数
    private int score = 0;
    // 最高分
    private int highScore = 0;
    // 分数标签
    private JLabel scoreLabel;
    // 状态标签
    private JLabel statusLabel;
    // 游戏级别
    private int level = 1;
    
    // 颜色数组，用于不同大小的物体
    private static final Color[] COLORS = {
        new Color(255, 204, 204),
        new Color(255, 153, 153),
        new Color(255, 102, 102),
        new Color(255, 51, 51),
        new Color(255, 0, 0),
        new Color(204, 0, 0),
        new Color(153, 0, 0),
        new Color(102, 0, 0),
        new Color(51, 0, 0),
        new Color(0, 0, 0),
        new Color(0, 51, 0),
        new Color(0, 102, 0),
        new Color(0, 153, 0),
        new Color(0, 204, 0),
        new Color(0, 255, 0),
        new Color(51, 255, 51),
        new Color(102, 255, 102),
        new Color(153, 255, 153),
        new Color(204, 255, 204),
        new Color(255, 255, 204),
        new Color(255, 255, 153),
        new Color(255, 255, 102),
        new Color(255, 255, 51),
        new Color(255, 255, 0),
        new Color(255, 204, 0),
        new Color(255, 153, 0),
        new Color(255, 102, 0),
        new Color(255, 51, 0),
        new Color(255, 0, 0),
        new Color(204, 0, 0),
        new Color(153, 0, 0),
        new Color(102, 0, 0),
        new Color(51, 0, 0),
        new Color(0, 0, 0),
        new Color(0, 0, 51),
        new Color(0, 0, 102),
        new Color(0, 0, 153),
        new Color(0, 0, 204),
        new Color(0, 0, 255),
        new Color(51, 51, 255),
        new Color(102, 102, 255),
        new Color(153, 153, 255),
        new Color(204, 204, 255),
        new Color(255, 204, 255),
        new Color(255, 153, 255),
        new Color(255, 102, 255),
        new Color(255, 51, 255),
        new Color(255, 0, 255),
        new Color(204, 0, 204),
        new Color(153, 0, 153),
        new Color(102, 0, 102),
        new Color(51, 0, 51),
        new Color(102, 0, 153),
        new Color(153, 0, 204),
        new Color(204, 0, 255),
        new Color(0, 102, 204),
        new Color(0, 153, 255),
        new Color(0, 204, 255),
        new Color(0, 255, 255),
        new Color(51, 255, 255),
        new Color(102, 255, 255),
        new Color(153, 255, 255),
        new Color(204, 255, 255),
        new Color(255, 255, 224),
        new Color(255, 255, 192),
        new Color(255, 255, 160),
        new Color(255, 255, 128),
        new Color(255, 248, 220),
        new Color(255, 245, 238),
        new Color(255, 235, 205),
        new Color(255, 228, 181),
        new Color(255, 222, 173),
        new Color(255, 218, 185),
        new Color(255, 215, 0),
        new Color(255, 211, 155),
        new Color(255, 206, 135),
        new Color(255, 201, 115),
        new Color(255, 196, 95),
        new Color(255, 191, 75),
        new Color(255, 186, 55),
        new Color(255, 181, 35),
        new Color(255, 176, 15),
        new Color(255, 171, 0)
    };

    // 水果图片资源
    private static final String[] FRUIT_IMAGES = {
        "https://s1.aigei.com/src/img/png/51/51e149664e1d479990091141d654a8b0.png?imageMogr2/auto-orient/thumbnail/!282x282r/gravity/Center/crop/282x282/quality/85/%7CimageView2/2/w/282&e=2051020800&token=P7S2Xpzfz11vAkASLTkfHN7Fw-oOZBecqeJaxypL:CoolXAeOBycg-bq34JXvZ4mhhJo=",
        "https://s1.aigei.com/src/img/png/8e/8ee629a2a51c48cfb05d67bb939cf617.png?imageMogr2/auto-orient/thumbnail/!282x282r/gravity/Center/crop/282x282/quality/85/%7CimageView2/2/w/282&e=2051020800&token=P7S2Xpzfz11vAkASLTkfHN7Fw-oOZBecqeJaxypL:3RjN9F4tIp9eg5_dz5zU5lgcSZc=",
        "https://s1.aigei.com/src/img/png/cc/ccda3930485f474ea35b1e5354aad2d1.png?imageMogr2/auto-orient/thumbnail/!282x282r/gravity/Center/crop/282x282/quality/85/%7CimageView2/2/w/282&e=2051020800&token=P7S2Xpzfz11vAkASLTkfHN7Fw-oOZBecqeJaxypL:rBk7Xf1Ld_GX5zfLVFAF3xgoDdM=",
        "https://s1.aigei.com/src/img/png/f1/f127a730e29c44488157446f3b56f2a5.png?imageMogr2/auto-orient/thumbnail/!240x320r/gravity/Center/crop/240x320/quality/85/%7CimageView2/2/w/240&e=2051020800&token=P7S2Xpzfz11vAkASLTkfHN7Fw-oOZBecqeJaxypL:p6quSXjD5_sQDfZu3sZTQQc2Y0M=",
        "https://s1.chu0.com/pvimg/img/png/57/574365e160e04f50be79a28ac9f1c6c9.png?imageMogr2/auto-orient/thumbnail/!240x320r/gravity/Center/crop/240x320/quality/85/%7CimageView2/2/w/240&e=2051020800&token=1srnZGLKZ0Aqlz6dk7yF4SkiYf4eP-YrEOdM1sob:HWiY4rOA_B2W_OtxFmHgyncKPQg=",
        "https://s1.aigei.com/src/img/png/c1/c1fc8a32ecd849e8a1c9fa9e96b57355.png?imageMogr2/auto-orient/thumbnail/!282x282r/gravity/Center/crop/282x282/quality/85/%7CimageView2/2/w/282&e=2051020800&token=P7S2Xpzfz11vAkASLTkfHN7Fw-oOZBecqeJaxypL:rzFKLdsWjFskMockv_R15rAF4Bc=",
        "https://s1.aigei.com/src/img/png/27/27b2eac1cc0942539f533892605e7ff8.png?imageMogr2/auto-orient/thumbnail/!282x282r/gravity/Center/crop/282x282/quality/85/%7CimageView2/2/w/282&e=2051020800&token=P7S2Xpzfz11vAkASLTkfHN7Fw-oOZBecqeJaxypL:K-NJlal0sWKn5_RckECVdD3ZzeM=",
        "https://s1.aigei.com/src/img/png/da/da7483ff899e409faf56f3c8a7e87b24.png?imageMogr2/auto-orient/thumbnail/!282x282r/gravity/Center/crop/282x282/quality/85/%7CimageView2/2/w/282&e=2051020800&token=P7S2Xpzfz11vAkASLTkfHN7Fw-oOZBecqeJaxypL:u7w2AD6bZVmnwBvIGu5-quPwz9o=",
        "https://s1.aigei.com/src/img/png/4d/4dcc0472dbd5494b81becfff169ef7db.png?imageMogr2/auto-orient/thumbnail/!282x282r/gravity/Center/crop/282x282/quality/85/%7CimageView2/2/w/282&e=2051020800&token=P7S2Xpzfz11vAkASLTkfHN7Fw-oOZBecqeJaxypL:FpuydhKFHVlkMESD4IqcRJPN9PA=",
        "https://s1.aigei.com/src/img/png/80/8087ba932f5b4969a03c7f834a7f95cd.png?imageMogr2/auto-orient/thumbnail/!282x282r/gravity/Center/crop/282x282/quality/85/%7CimageView2/2/w/282&e=2051020800&token=P7S2Xpzfz11vAkASLTkfHN7Fw-oOZBecqeJaxypL:cFg4RaeEDsWSCo-4RVz80bSKovQ=",
        "https://s1.aigei.com/src/img/png/2f/2fb77efe3ddf42fe991fba247d993e55.png?imageMogr2/auto-orient/thumbnail/!240x320r/gravity/Center/crop/240x320/quality/85/%7CimageView2/2/w/240&e=2051020800&token=P7S2Xpzfz11vAkASLTkfHN7Fw-oOZBecqeJaxypL:H6ag-wCbtZqFhVaEpoLIzrrajFo=",
        "https://s1.aigei.com/src/img/png/37/370fd909d8f34b15ab60855dd1a0630f.png?imageMogr2/auto-orient/thumbnail/!282x282r/gravity/Center/crop/282x282/quality/85/%7CimageView2/2/w/282&e=2051020800&token=P7S2Xpzfz11vAkASLTkfHN7Fw-oOZBecqeJaxypL:2durHTy4AN85PMa9TNqtQodzgTE=",
        "https://s1.aigei.com/src/img/gif/85/854b8f71b3fd48e099ff631578a0500f.gif?imageMogr2/auto-orient/thumbnail/!282x282r/gravity/Center/crop/282x282/quality/85/%7CimageView2/2/w/282&e=2051020800&token=P7S2Xpzfz11vAkASLTkfHN7Fw-oOZBecqeJaxypL:ZSBy8IeDma9dm7bu5XLhjh24w5k="
    };

    public MergeWatermelonGame() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.WHITE);
        
        // 添加键盘监听器
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_SPACE && currentObject != null) {
                    // 空格键释放当前物体
                    currentObject.waiting = false;
                    currentObject = null;
                    nextNewObject();
                } else if (keyCode == KeyEvent.VK_LEFT && currentObject != null) {
                    // 左箭头移动当前物体
                    currentObject.x -= 15;
                    if (currentObject.x < 0) {
                        currentObject.x = 0;
                    }
                } else if (keyCode == KeyEvent.VK_RIGHT && currentObject != null) {
                    // 右箭头移动当前物体
                    currentObject.x += 15;
                    if (currentObject.x > WINDOW_WIDTH - currentObject.size) {
                        currentObject.x = WINDOW_WIDTH - currentObject.size;
                    }
                } else if (keyCode == KeyEvent.VK_R && (gameOver || gameWin)) {
                    // R键重新开始游戏
                    restartGame();
                } else if (keyCode == KeyEvent.VK_P) {
                    // P键暂停/继续游戏
                    isPaused = !isPaused;
                    updateStatusLabel();
                }
            }
        });
    }

    // 初始化方法
    public void initialize() {
        // 清空物体列表
        objectList.clear();
        
        // 创建初始物体 - 设置初始大小为60
        newObject(60);
        currentObject = objectList.get(0);
        currentObject.waiting = true;
        
        // 更新状态标签
        updateStatusLabel();
        
        // 启动游戏循环定时器
        Timer timer = new Timer(16, e -> {
            if (!isPaused && !gameOver && !gameWin) {
                gameOperation();
            }
            repaint();
        });
        timer.start();
    }
    
    // 更新状态标签
    private void updateStatusLabel() {
        if (statusLabel != null) {
            String status = "";
            if (gameOver) {
                status = "Game Over. Press R to restart";
            } else if (gameWin) {
                status = "You win! Press R to restart";
            } else if (isPaused) {
                status = "Game is paused，Press P to Continue";
            } else {
                status = "Current LV: " + level + "  Arrow Key to move, SPACE to drop";
            }
            statusLabel.setText(status);
        }
    }

    // 计算两点距离
    private double distance(GameObject a, GameObject b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    // 精确计算球体顶部位置
    private double getTopY(GameObject obj) {
        return obj.y; // 球体的y坐标已经是顶部位置
    }

    // 精确计算球体底部位置
    private double getBottomY(GameObject obj) {
        return obj.y + obj.size; // 底部位置 = y + 直径
    }

    // 精确计算球体左边界位置
    private double getLeftX(GameObject obj) {
        return obj.x; // 左边界位置 = x
    }

    // 精确计算球体右边界位置
    private double getRightX(GameObject obj) {
        return obj.x + obj.size; // 右边界位置 = x + 直径
    }

    // 失败判断 - 只有当堆叠的球体触碰到顶部时才失败
    private boolean isGameOver() {
        for (GameObject object : objectList) {
            if (!object.waiting && getTopY(object) <= 0 && isStacked(object)) {
                return true;
            }
        }
        return false;
    }

    // 检查球体是否被其他球体支撑着（堆叠）
    private boolean isStacked(GameObject obj) {
        // 如果球体正在下落或有向上的速度，不认为是堆叠
        if (Math.abs(obj.vy) > 0.1) {
            return false;
        }
        
        // 检查是否有其他球体在它下面支撑它
        for (GameObject other : objectList) {
            if (other == obj || other.waiting) continue;
            
            // 如果other球体在obj球体的正下方，并且距离足够近
            double horizontalDist = Math.abs((obj.x + obj.size/2) - (other.x + other.size/2));
            double verticalDist = getBottomY(obj) - getTopY(other);
            
            // 如果水平距离小于两个球体半径之和，并且垂直距离小于一个小阈值
            if (horizontalDist < (obj.size/2 + other.size/2) && verticalDist < 5) {
                return true;
            }
        }
        
        // 如果没有找到支撑的球体，返回false
        return false;
    }

    // 生成新物体 - 只生成大于等于60的10的倍数的球
    private void newObject(int size) {
        // 确保生成的球大小是10的倍数且大于等于60
        if (size < 60) size = 60;
        if (size % 10 != 0) size = ((size / 10) + 1) * 10;
        
        // 调整物体的初始位置，确保在窗口内
        double x = (WINDOW_WIDTH - size) / 2.0;
        double y = 50;
        double vx = 0;
        double vy = 0;
        double angle = 0;
        boolean waiting = true;
        // 新增：初始状态不是静止
        boolean isResting = false;
        objectList.add(new GameObject(size, x, y, vx, vy, angle, waiting, isResting));
    }

    // 下一个新物体 - 只生成大于等于60的10的倍数的球
    private void nextNewObject() {
        int maxSize = 60; // 初始最小为60
        for (GameObject object : objectList) {
            if (object.size > maxSize) {
                maxSize = object.size;
            }
        }
        
        // 级别提升逻辑
        if (maxSize >= 120) {
            level = 2;
        }
        if (maxSize >= 180) {
            level = 3;
        }
        if (maxSize >= 240) {
            level = 4;
        }
        
        if (maxSize > 300) { // 限制最大尺寸为300
            maxSize = 300;
        }
        
        // 生成大于等于60的10的倍数的球
        // 随机生成一个60到maxSize之间的10的倍数
        int newSize = 60 + (int)(Math.random() * ((maxSize - 60) / 10 + 1)) * 10;
        
        newObject(newSize);
        currentObject = objectList.get(objectList.size() - 1);
        currentObject.waiting = true;
        
        gameOver = isGameOver();
        if (gameOver) {
            updateStatusLabel();
        }
    }

    // 重新开始游戏
    private void restartGame() {
        if (score > highScore) {
            highScore = score;
        }
        score = 0;
        level = 1;
        gameOver = false;
        gameWin = false;
        initialize();
    }

    // 游戏运作逻辑
    private void gameOperation() {
        for (int i = 0; i < objectList.size(); i++) {
            GameObject object = objectList.get(i);
            if (object.waiting || object == currentObject) {
                continue;
            }
            
            // 如果物体处于静止状态，跳过物理更新
            if (object.isResting) {
                continue;
            }
            
            // 应用重力
            object.vy += GRAVITY;
            
            // 检测与其他物体的碰撞
            for (int j = 0; j < objectList.size(); j++) {
                if(j == i)
                    continue;
                GameObject compareObject = objectList.get(j); 
                if (compareObject.waiting || compareObject == currentObject) {
                    continue;
                }
                
                // 如果两个物体都静止，则不检测碰撞
                if (object.isResting && compareObject.isResting) {
                    continue;
                }
                
                double dist = distance(object, compareObject);
                double minDist = (object.size / 2 + compareObject.size / 2);
                
                if (dist <= minDist) {
                    // 合并逻辑 - 只有相同大小的物体才能合并
                    if (object.size == compareObject.size) {
                        // 计算合并后的新位置
                        double newX = (object.x + compareObject.x) / 2;
                        double newY = (object.y + compareObject.y) / 2;
                        
                        // 检查合并后的新球是否会导致游戏结束
                        if (newY <= 0) {
                            gameOver = true;
                            updateStatusLabel();
                            return;
                        }
                        
                        // 确保合并后的球体不会超出边界
                        if (newX < 0) newX = 0;
                        if (newX + object.size > WINDOW_WIDTH) newX = WINDOW_WIDTH - object.size;
                        if (newY < 0) newY = 0;
                        if (newY + object.size > WINDOW_HEIGHT) newY = WINDOW_HEIGHT - object.size;
                        
                        // 正常合并
                        int newSize = object.size + 10;
                        object.size = newSize;
                        object.x = newX;
                        object.y = newY;
                        object.vx = (object.vx + compareObject.vx) / 4;
                        object.vy = (object.vy + compareObject.vy) / 4;
                        // 合并后物体不再静止
                        object.isResting = false;
                        
                        // 限制最大速度，防止下一帧直接穿过边界
                        double maxSpeed = 5.0;
                        if (object.vy > maxSpeed) object.vy = maxSpeed;
                        
                        objectList.remove(j);
                        j--;
                        
                        // 合并时加分
                        score += newSize;
                        scoreLabel.setText("SCORE: " + score + "  Highest Score: " + highScore);
                        
                        // 检查是否达到胜利条件
                        if (newSize >= 300) {
                            gameWin = true;
                            updateStatusLabel();
                        }
                    } else {
                        // 碰撞反弹 - 不同大小的球碰撞
                        double dx = compareObject.x - object.x;
                        double dy = compareObject.y - object.y;
                        double centerDistance = Math.sqrt(dx * dx + dy * dy);
                        double actualDistance = centerDistance - (object.size/2 + compareObject.size/2);
                        
                        if (actualDistance <= 0) {
                            double nx = dx / centerDistance;
                            double ny = dy / centerDistance;
                            
                            // 计算相对速度
                            double dvx = object.vx - compareObject.vx;
                            double dvy = object.vy - compareObject.vy;
                            
                            // 计算相对速度在法线方向上的投影
                            double velocityAlongNormal = dvx * nx + dvy * ny;
                            
                            // 碰撞响应 - 基于动量守恒
                            double mass1 = object.size * object.size;  // 假设质量与面积成正比
                            double mass2 = compareObject.size * compareObject.size;
                            
                            // 计算碰撞后的速度分量
                            double impulse = -1.0 * velocityAlongNormal / (1/mass1 + 1/mass2);
                            
                            // 基于球体大小调整反弹力度
                            double sizeFactor = Math.min(object.size, compareObject.size) / 10.0;
                            sizeFactor = Math.max(2.0, sizeFactor);  // 确保最小为2.0
                            
                            // 应用反弹力
                            object.vx += impulse * nx * 0.8 / mass1;
                            object.vy += impulse * ny * sizeFactor / mass1;
                            compareObject.vx -= impulse * nx * 0.8 / mass2;
                            compareObject.vy -= impulse * ny * sizeFactor / mass2;
                            
                            // 碰撞后两个物体都不再静止
                            object.isResting = false;
                            compareObject.isResting = false;
                            
                            // 检测并修正位置，防止球体重叠
                            double overlap = minDist - dist;
                            if (overlap > 0) {
                                // 根据质量比例分配重叠修正量
                                double totalMass = mass1 + mass2;
                                double ratio1 = mass1 / totalMass;
                                double ratio2 = mass2 / totalMass;
                                
                                // 调整位置，防止球体重叠
                                object.x -= nx * overlap * ratio2;
                                object.y -= ny * overlap * ratio2;
                                compareObject.x += nx * overlap * ratio1;
                                compareObject.y += ny * overlap * ratio1;
                            }
                        }
                    }
                }
            }
            
            // 边界处理 - 防止球体被挤出边界
            boolean hasBounced = false;
            
            // 左右边界处理
            if (getLeftX(object) < 0) {
                object.x = 0; // 确保球体左边界刚好接触左边界
                object.vx = -object.vx * BOUNCE_DAMPING;
                hasBounced = true;
                // 边界碰撞后不再静止
                object.isResting = false;
            } else if (getRightX(object) > WINDOW_WIDTH) {
                object.x = WINDOW_WIDTH - object.size; // 确保球体右边界刚好接触右边界
                object.vx = -object.vx * BOUNCE_DAMPING;
                hasBounced = true;
                // 边界碰撞后不再静止
                object.isResting = false;
            }
            
            // 顶部边界处理
            if (getTopY(object) < 0) {
                object.y = 0; // 确保球体顶部刚好接触顶部边界
                object.vy = -object.vy * BOUNCE_DAMPING;
                hasBounced = true;
                // 边界碰撞后不再静止
                object.isResting = false;
            } 
            // 底部边界处理
            else if (getBottomY(object) > WINDOW_HEIGHT) {
                // 正确设置小球底部接触窗口底部
                object.y = WINDOW_HEIGHT - object.size; 
                
                // 检测是否应该进入静止状态
                if (Math.abs(object.vy) <= 0.2 ) {
                    object.y = 0;
                    object.vy = 0;
                    object.isResting = true;  // 标记为静止状态
                } else {
                    // 速度较大时反弹
                    object.vy = -object.vy * BOUNCE_DAMPING;
                    hasBounced = true;
                    object.isResting = false;
                }
            }
            
            // 如果没有发生碰撞，应用阻尼
            if (!hasBounced) {
                object.vx *= DAMPING;
                object.vy *= DAMPING;
                
                // 额外检查是否应该进入静止状态
                if (!object.isResting && Math.abs(object.vy) <= 0.1 && Math.abs(object.vx) <= 0.1) {
                    // 检查是否在地面或其他物体上
                    boolean onGround = getBottomY(object) >= WINDOW_HEIGHT - 1;
                    boolean onObject = false;
                    
                    if (!onGround) {
                        // 检查是否在其他物体上
                        for (GameObject other : objectList) {
                            if (other == object || other.waiting) continue;
                            
                            double horizontalDist = Math.abs((object.x + object.size/2) - (other.x + other.size/2));
                            double verticalDist = getBottomY(object) - getTopY(other);
                            
                            if (horizontalDist < (object.size/2 + other.size/2) && verticalDist < 5) {
                                onObject = true;
                                break;
                            }
                        }
                    }
                    
                    if (onGround || onObject) {
                        object.vy = 0;
                        object.vx = 0;
                        object.isResting = true;
                    }
                }
            }
            
            // 限制最大速度，防止下一帧直接穿过边界
            double maxSpeed = 15.0;
            if (object.vy > maxSpeed) object.vy = maxSpeed;
            if (Math.abs(object.vx) > maxSpeed) object.vx = Math.copySign(maxSpeed, object.vx);
            
            // 更新位置
            if (!object.isResting) {
                object.x += object.vx;
                object.y += object.vy;
            }
        }
        
        // 最后检查游戏是否结束
        if (!gameOver) {
            gameOver = isGameOver();
            if (gameOver) {
                updateStatusLabel();
            }
        }
    }

    // 绘制游戏画面
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // 启用抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 绘制网格背景
        g2d.setColor(new Color(240, 240, 240));
        int gridSize = 20;
        for (int x = 0; x < WINDOW_WIDTH; x += gridSize) {
            for (int y = 0; y < WINDOW_HEIGHT; y += gridSize) {
                g2d.fillRect(x, y, gridSize - 1, gridSize - 1);
            }
        }
        
        // 绘制物体
        for (GameObject object : objectList) {
            // 绘制图片或颜色圆
            if (object.image != null) {
                g2d.drawImage(object.image, (int)object.x, (int)object.y, 
                             object.size, object.size, null);
            } else {
                // 根据物体大小选择颜色
                int colorIndex = Math.min((object.size - 60) / 5, COLORS.length - 1);
                if (colorIndex < 0) colorIndex = 0;
                g2d.setColor(COLORS[colorIndex]);
                
                Ellipse2D.Double circle = new Ellipse2D.Double(
                        object.x,
                        object.y,
                        object.size,
                        object.size
                );
                g2d.fill(circle);
                
                // 绘制边框
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(Math.max(1.0f, object.size / 30.0f)));
                g2d.draw(circle);
            }
            
            // 绘制物体大小文本
            FontMetrics metrics = g2d.getFontMetrics();
            String text = String.valueOf(object.size);
            int textWidth = metrics.stringWidth(text);
            int textHeight = metrics.getHeight();
            
            g2d.setColor(Color.WHITE);
            // 调整字体大小，确保大球上的数字清晰可见
            g2d.setFont(new Font("Arial", Font.BOLD, Math.max(14, object.size / 10)));
            g2d.drawString(text, 
                    (int) (object.x + object.size / 2 - textWidth / 2), 
                    (int) (object.y + object.size / 2 + textHeight / 4));
        }
        
        // 绘制游戏状态
        if (gameOver) {
            g2d.setColor(new Color(255, 0, 0, 128));
            g2d.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            FontMetrics metrics = g2d.getFontMetrics();
            String text = "Game Over!";
            int textWidth = metrics.stringWidth(text);
            g2d.drawString(text, (WINDOW_WIDTH - textWidth) / 2, WINDOW_HEIGHT / 2 - 30);
            
            text = "Final Score: " + score;
            textWidth = metrics.stringWidth(text);
            g2d.drawString(text, (WINDOW_WIDTH - textWidth) / 2, WINDOW_HEIGHT / 2 + 30);
        } else if (gameWin) {
            g2d.setColor(new Color(0, 200, 0, 128));
            g2d.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            FontMetrics metrics = g2d.getFontMetrics();
            String text = "恭喜你赢了!";
            int textWidth = metrics.stringWidth(text);
            g2d.drawString(text, (WINDOW_WIDTH - textWidth) / 2, WINDOW_HEIGHT / 2 - 30);
            
            text = "Final Score: " + score;
            textWidth = metrics.stringWidth(text);
            g2d.drawString(text, (WINDOW_WIDTH - textWidth) / 2, WINDOW_HEIGHT / 2 + 30);
        } else if (isPaused) {
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            FontMetrics metrics = g2d.getFontMetrics();
            String text = "Paused";
            int textWidth = metrics.stringWidth(text);
            g2d.drawString(text, (WINDOW_WIDTH - textWidth) / 2, WINDOW_HEIGHT / 2);
        }
    }

    // 主方法启动游戏窗口
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MergeWatermelon-Java");
            MergeWatermelonGame gamePanel = new MergeWatermelonGame();
            
            // 设置高分屏字体缩放
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            frame.add(gamePanel);
            
            // 创建并添加分数标签
            JPanel controlPanel = new JPanel(new BorderLayout());
            gamePanel.scoreLabel = new JLabel("Score: 0  Highest: 0");
            gamePanel.scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
            controlPanel.add(gamePanel.scoreLabel, BorderLayout.WEST);
            
            // 创建并添加状态标签
            gamePanel.statusLabel = new JLabel("Current LV: 1  Arrow Key to move，SPACE to drop");
            gamePanel.statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            controlPanel.add(gamePanel.statusLabel, BorderLayout.EAST);
            
            frame.add(controlPanel, BorderLayout.NORTH);
            
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            
            gamePanel.initialize();
        });
    }

    // 定义物体类
    private class GameObject {
        int size;
        double x;
        double y;
        double vx;
        double vy;
        double angle;
        boolean waiting;
        boolean isResting;
        Image image;  // 图片资源

        public GameObject(int size, double x, double y, double vx, double vy, 
                         double angle, boolean waiting, boolean isResting) {
            this.size = size;
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.angle = angle;
            this.waiting = waiting;
            this.isResting = isResting;
            
            // 尝试加载对应的水果图片
           // 尝试加载对应的水果图片
        // 尝试加载对应的水果图片
        try {
         int imgIndex = Math.min((size - 60) / 10, FRUIT_IMAGES.length - 1);
         URL url = new URL(FRUIT_IMAGES[imgIndex]);
         image = ImageIO.read(url);
    
         // 调整图片大小以匹配球体尺寸
        if (image != null) {
           image = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        }
        } catch (IOException e) {
          e.printStackTrace();
    // 图片加载失败时使用默认颜色（已在绘制时处理）
        }
    }
}
}