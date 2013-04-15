package virtualposition;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;

public class VirtualPositionHelper {
    private Point localRoot;
    private Integer networkWidth;
    private ArrayList<Point> pointsAvailable;

    public VirtualPositionHelper(Point localRoot) {
        super();
        this.localRoot = localRoot;
        this.networkWidth = 1;
        while(this.networkWidth <= localRoot.x && this.networkWidth <= localRoot.y) {
            this.networkWidth *= 2;
        }
        this.pointsAvailable = new ArrayList<Point>();
    }
    
    public Point getLocalRoot() {
        return localRoot;
    }
    
    public Integer getNetworkWidth() {
        return networkWidth;
    }
    
    public Integer getNetworkStepSize() {
        return networkWidth / 2;
    }
    
    public Point getNorthPosition(Point pos) {
        pos.y = (pos.y + 1) % networkWidth;
        return pos;
    }
    
    public Point getEastPosition(Point pos) {
        pos.x = (pos.x + 1) % networkWidth;
        return pos;
    }
    
    public Point getSouthPosition(Point pos) {
        pos.y = (pos.y - 1) % networkWidth;
        return pos;
    }
    
    public Point getWestPosition(Point pos) {
        pos.x = (pos.x - 1) % networkWidth;
        return pos;
    }
    
    public Point popVirtualPosition() {
        if(this.pointsAvailable.size() == 0) return null;
        return this.pointsAvailable.remove(0);
    }
    
    public void pushVirtualPosition(Point pos) {
        this.pointsAvailable.add(pos);
    }
    
    public Collection<Point> getPointsNodeIsResponsibleFor() {
        ArrayList<Point> positions = new ArrayList<Point>();
        positions.addAll(pointsAvailable);
        positions.add(localRoot);
        return positions;
    }
    
    public Integer IncreaseNetworkSize() {
        this.networkWidth *= 2;
        pointsAvailable.addAll(VirtualPositionHelper.getPointsInheritedForStepSize(localRoot, this.getNetworkStepSize()));
        return this.networkWidth;
    }
    
    private static Collection<Point> getPointsInheritedForStepSize(Point pos, Integer stepSize) {
        ArrayList<Point> points = new ArrayList<Point>();
        points.add(new Point(pos.x + stepSize, pos.y));
        points.add(new Point(pos.x + stepSize, pos.y + stepSize));
        points.add(new Point(pos.x, pos.y + stepSize));
        return points;
    }
}
