package simulation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.PriorityQueue;

import javax.swing.Timer;

import components.ComponentManager;

public class NetworkSim {
    private long tickCounter;
    private long limit;
    private ComponentManager componentManager;
    private Timer timer;
    private PriorityQueue<ScheduledAction> actions;
    
    public NetworkSim() {
        this.tickCounter = 0;
        this.limit = 100;
        this.componentManager = null;
        this.timer = new Timer(50, new Tick(this));
        this.actions = new PriorityQueue<ScheduledAction>(10, new ActionCompare());
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public void setComponentManager(ComponentManager componentManager) {
        this.componentManager = componentManager;
    }
    
    private class Tick implements ActionListener {
        private NetworkSim sim;
        public Tick(NetworkSim sim) {
            this.sim = sim;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            sim.tick();
        }
        
    }
    
    private class ActionCompare implements Comparator<ScheduledAction> {
        @Override
        public int compare(ScheduledAction o1, ScheduledAction o2) {
            return o1.getTick() - o2.getTick();
        }
    }

    public void tick() {
        ++tickCounter;
        do {
            if(!actions.isEmpty() && actions.peek().getTick() == tickCounter) {
                // Do action
                if(actions.peek().getAction() == ScheduledAction.ScheduledActionType.stopSim) {
                    actions.clear();
                    this.timer.stop();
                } else {
                    this.componentManager.performAction(actions.poll());
                }
            }
        } while(!actions.isEmpty() && actions.peek().getTick() == tickCounter);
        this.componentManager.tick();
    }
    
    public void simulate() {
        tickCounter = 0;
        this.componentManager.initializeNetwork();
        this.timer.start();
    }
    
    public void addAction(ScheduledAction action) {
        this.actions.add(action);
    }
}
