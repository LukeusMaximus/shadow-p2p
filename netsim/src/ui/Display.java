package ui;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Display extends JFrame {
    
    private static final int initWidth = 1280;
    private static final int initHeight = 1024;
    
    private DrawArea drawArea;
    
    public Display() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setSize(initWidth, initHeight);
        this.setTitle("NetSim");
        drawArea = new DrawArea(initWidth, initHeight);
        this.setContentPane(drawArea);
    }
    
    public DrawArea getDrawArea() {
        return drawArea;
    }
}
