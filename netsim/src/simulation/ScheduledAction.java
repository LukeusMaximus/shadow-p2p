package simulation;

import java.awt.Point;

public class ScheduledAction {
    private Integer tick;
    private ScheduledActionType action;
    private Point nodeParam;
    
    public ScheduledAction(Integer tick, ScheduledActionType type) {
        this.action = type;
        this.tick = tick;
    }
    
    public Integer getTick() {
        return tick;
    }

    public ScheduledActionType getAction() {
        return action;
    }

    public Point getNodeParam() {
        return nodeParam;
    }

    public void setNodeParam(Point nodeParam) {
        this.nodeParam = nodeParam;
    }

    public enum ScheduledActionType {
        nodejoin,
        nodeleave,
        attemptContraction,
        sendDummy,
        stopSim
    }
}
