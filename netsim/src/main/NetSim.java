package main;

import java.awt.Point;
import java.security.Security;

import javax.swing.JFrame;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import simulation.NetworkSim;
import simulation.ScheduledAction;
import simulation.ScheduledAction.ScheduledActionType;
import ui.DrawArea;

import components.ComponentManager;

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
        action = new ScheduledAction(60, ScheduledActionType.nodejoin);
        action.setNodeParam(new Point(0,0));
        simulation.addAction(action);
        action = new ScheduledAction(100, ScheduledActionType.nodejoin);
        action.setNodeParam(new Point(0,0));
        simulation.addAction(action);
        action = new ScheduledAction(140, ScheduledActionType.nodejoin);
        action.setNodeParam(new Point(0,0));
        simulation.addAction(action);
        action = new ScheduledAction(180, ScheduledActionType.nodejoin);
        action.setNodeParam(new Point(0,0));
        simulation.addAction(action);
        action = new ScheduledAction(220, ScheduledActionType.nodejoin);
        action.setNodeParam(new Point(0,0));
        simulation.addAction(action);
        action = new ScheduledAction(260, ScheduledActionType.nodejoin);
        action.setNodeParam(new Point(0,0));
        simulation.addAction(action);
        action = new ScheduledAction(400, ScheduledActionType.stopSim);
        action.setNodeParam(new Point(0,0));
        simulation.addAction(action);
        simulation.simulate();
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        NetSim sim = new NetSim();
        sim.start();
    }

}
