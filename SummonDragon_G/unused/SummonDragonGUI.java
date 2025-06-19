package unused;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.Arrays;
import javax.swing.*;

public class SummonDragonGUI {
    private static final String[] LEVELS = { "螃蟹", "乌龟", "鹤", "鱼", "蛙", "蛇", "神龙" };
    private static final int[] INIT_COUNTS = { 8, 0, 0, 0, 0, 0, 0 };
    private static final Color BG_COLOR = new Color(240, 255, 240);

    private int[] counts;
    private int selectedLevel = -1;
    private JFrame frame;
    private JLabel[] countLabels;
    private JLabel statusLabel;

    public SummonDragonGUI() {
        counts = Arrays.copyOf(INIT_COUNTS, INIT_COUNTS.length);
        initializeGUI();
    }

    private void initializeGUI() {
        frame = new JFrame("召唤神龙");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // 游戏区域面板
        JPanel gamePanel = new JPanel(new GridLayout(2, 4, 20, 20));
        gamePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        gamePanel.setBackground(BG_COLOR);

        countLabels = new JLabel[LEVELS.length];
        for (int i = 0; i < LEVELS.length; i++) {
            JPanel card = createAnimalCard(i);
            gamePanel.add(card);
        }

        // 状态栏
        statusLabel = new JLabel("点击两个相同的动物进行合成", JLabel.CENTER);
        statusLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        frame.add(gamePanel, BorderLayout.CENTER);
        frame.add(statusLabel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private JPanel createAnimalCard(int level) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // 加载图片（需要准备对应图片文件）
        ImageIcon icon = loadImage(LEVELS[level] + ".png");
        JLabel imageLabel = new JLabel(icon);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);

        // 等级标签
        JLabel levelLabel = new JLabel("Lv." + level, JLabel.CENTER);
        levelLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));

        // 数量标签
        countLabels[level] = new JLabel("×" + counts[level], JLabel.CENTER);
        countLabels[level].setFont(new Font("微软雅黑", Font.BOLD, 24));
        countLabels[level].setForeground(new Color(0, 100, 0));

        panel.add(levelLabel, BorderLayout.NORTH);
        panel.add(imageLabel, BorderLayout.CENTER);
        panel.add(countLabels[level], BorderLayout.SOUTH);

        // 添加点击监听
        panel.addMouseListener(new AnimalClickListener(level));
        return panel;
    }

    private ImageIcon loadImage(String filename) {
        try {
            URL imgUrl = getClass().getResource("/images/" + filename);
            if (imgUrl != null) {
                ImageIcon icon = new ImageIcon(imgUrl);
                Image scaled = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ImageIcon(); // 返回空图标
    }

    private class AnimalClickListener extends MouseAdapter {
        private final int level;

        public AnimalClickListener(int level) {
            this.level = level;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (counts[level] < 1)
                return;

            if (selectedLevel == -1) { // 第一次选择
                selectedLevel = level;
                statusLabel.setText("已选择 " + LEVELS[level] + "，请选择第二个");
            } else { // 第二次选择
                if (level == selectedLevel) {
                    attemptMerge(level);
                } else {
                    statusLabel.setText("必须选择相同等级的动物！");
                }
                selectedLevel = -1; // 重置选择
            }
        }
    }

    private void attemptMerge(int level) {
        if (level >= LEVELS.length - 1) {
            statusLabel.setText("神龙已经是最高等级！");
            return;
        }

        if (counts[level] >= 2) {
            counts[level] -= 2;
            counts[level + 1]++;
            updateDisplay();

            String result = "合成成功！获得一只" + LEVELS[level + 1];
            statusLabel.setText(result);
            checkGameEnd();
        } else {
            statusLabel.setText("合成失败！" + LEVELS[level] + "数量不足");
        }
    }

    private void updateDisplay() {
        for (int i = 0; i < counts.length; i++) {
            countLabels[i].setText("×" + counts[i]);
        }
    }

    private void checkGameEnd() {
        if (counts[LEVELS.length - 1] > 0) {
            JOptionPane.showMessageDialog(frame, "恭喜你成功召唤神龙！", "游戏胜利",
                    JOptionPane.INFORMATION_MESSAGE);
            resetGame();
        } else if (!canMerge()) {
            JOptionPane.showMessageDialog(frame, "没有可以合成的动物了！", "游戏失败",
                    JOptionPane.WARNING_MESSAGE);
            resetGame();
        }
    }

    private boolean canMerge() {
        for (int i = 0; i < LEVELS.length - 1; i++) {
            if (counts[i] >= 2)
                return true;
        }
        return false;
    }

    private void resetGame() {
        counts = Arrays.copyOf(INIT_COUNTS, INIT_COUNTS.length);
        updateDisplay();
        statusLabel.setText("游戏已重置，点击两个相同的动物进行合成");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SummonDragonGUI();
        });
    }
}