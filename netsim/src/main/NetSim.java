package main;

import javax.swing.JFrame;

import ui.DrawArea;

public class NetSim {

    private static final int initWidth = 1280;
    private static final int initHeight = 1024;
    
    private JFrame frame;
    private DrawArea drawArea;
    
    public NetSim() {
        
    }
    
    public void start() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //this.setResizable(false);
        frame.setSize(initWidth, initHeight);
        frame.setTitle("NetSim");
        drawArea = new DrawArea(initWidth, initHeight);
        frame.setContentPane(drawArea);
        frame.setVisible(true);
        drawArea.drawCalibrationSquares();
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        NetSim sim = new NetSim();
        sim.start();
    }

}
