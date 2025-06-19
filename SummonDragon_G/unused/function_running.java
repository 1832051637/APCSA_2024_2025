package unused;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import javax.swing.*;

public class function_running{
    public static void main(String[] args) {
        // set background of the pond
        JFrame theGUI = new JFrame();
        theGUI.setSize(1000,1000);
        theGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel panel1 = new JPanel();
        panel1.setBackground(Color.cyan);
    
        
        Container pane = theGUI.getContentPane();
        pane.setLayout(new GridLayout(1,1));
        pane.add(panel1);

        theGUI.setVisible(true);
    }
}