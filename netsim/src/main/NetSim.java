package main;

import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JFrame;

import components.ComponentManager;

import simulation.NetworkSim;
import simulation.ScheduledAction;
import simulation.ScheduledAction.ScheduledActionType;
import ui.DrawArea;

public class NetSim {

    private static final int initWidth = 1280;
    private static final int initHeight = 1024;
    
    private JFrame frame;
    private DrawArea drawArea;
    private ComponentManager componentManager;
    private NetworkSim simulation;
    
    public NetSim() {
        
    }
    
    public void start() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(initWidth, initHeight);
        frame.setTitle("NetSim");
        
        drawArea = new DrawArea(initWidth, initHeight);
        componentManager = new ComponentManager();
        simulation = new NetworkSim();
        drawArea.setComponentManager(componentManager);
        simulation.setComponentManager(componentManager);
        componentManager.setDrawArea(drawArea);
        
        frame.setContentPane(drawArea);
        frame.setVisible(true);
        
        ScheduledAction action = new ScheduledAction(20, ScheduledActionType.nodejoin);
        action.setNodeParam(new Point(0,0));
        simulation.addAction(action);
        simulation.simulate();
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        NetSim sim = new NetSim();
        sim.start();
    }

    public void drawComponents(Graphics g) {
        // TODO Auto-generated method stub
        
    }

}
