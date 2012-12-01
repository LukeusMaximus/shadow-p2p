package main;

import ui.Display;
import ui.DrawArea;

public class NetSim {

    private Display display;
    private DrawArea drawArea;
    
    public NetSim() {
        
    }
    
    public void start() {
        display = new Display();
        display.setVisible(true);
        drawArea = display.getDrawArea();
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
