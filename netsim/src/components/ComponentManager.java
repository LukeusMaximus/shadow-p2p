package components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import simulation.ScheduledAction;
import ui.DrawArea;


public class ComponentManager {
    
    private static final Integer radius = 10;
    private static final Color nodeMainColour = new Color(128, 128, 255);
    private static final Color nodeOutlineColour = new Color(0, 0, 0);
    private static final Color linkColour = new Color(0, 0, 0);
    
    private DrawArea drawArea;
    private PacketShepard internet;
    
    public ComponentManager() {
        this.internet = new PacketShepard();
    }
    
    public void setDrawArea(DrawArea drawArea) {
        this.drawArea = drawArea;
    }

    public void renderComponents(Graphics g, Rectangle bounds) {
        int networkWidth = Node.getMaxWidth();
        double distx = bounds.width / networkWidth;
        double disty = bounds.height / networkWidth;
        Point drawPos, drawPos2;
        int i, j;
        /*
        // Horizontal
        for(i = 0; i < networkWidth-1; ++i) {
            for(j = 0; j < networkWidth; ++j) {
                Point nodePoint = new Point(i, j);
                Node.findNodeByPoint(nodePoint);
                drawPos = getNodeDrawPoint(distx, disty, nodePoint);
                ++nodePoint.x;
                drawPos2 = getNodeDrawPoint(distx, disty, nodePoint);
                g.setColor(linkColour);
                g.drawLine(drawPos.x, drawPos.y, drawPos2.x, drawPos2.y);
            }
        }
        // Horizontal trailing
        for(j = 0; j < networkWidth; ++j) {
            Point nodePoint = new Point(networkWidth-1, j);
            Node.findNodeByPoint(nodePoint);
            drawPos = getNodeDrawPoint(distx, disty, nodePoint);
            drawPos2 = drawPos;
            drawPos2.x += distx/2;
            g.setColor(linkColour);
            g.drawLine(drawPos.x, drawPos.y, drawPos2.x, drawPos2.y);
        }
        // Vertical
        for(i = 0; i < networkWidth; ++i) {
            for(j = 0; j < networkWidth-1; ++j) {
                Point nodePoint = new Point(i, j);
                Node.findNodeByPoint(nodePoint);
                drawPos = getNodeDrawPoint(distx, disty, nodePoint);
                ++nodePoint.y;
                drawPos2 = getNodeDrawPoint(distx, disty, nodePoint);
                g.setColor(linkColour);
                g.drawLine(drawPos.x, drawPos.y, drawPos2.x, drawPos2.y);
            }
        }
        // Vertical trailing
        for(i = 0; i < networkWidth; ++i) {
            Point nodePoint = new Point(i, networkWidth-1);
            Node.findNodeByPoint(nodePoint);
            drawPos = getNodeDrawPoint(distx, disty, nodePoint);
            drawPos2 = drawPos;
            drawPos2.y += distx/2;
            g.setColor(linkColour);
            g.drawLine(drawPos.x, drawPos.y, drawPos2.x, drawPos2.y);
        }*/
        // Nodes
        for(Node node : Node.nodesInNetwork) {
            for(Point virtPos : node.getPositionsResponsible()) {
                //System.out.println("Drawing " + virtPos.toString());
                drawPos = getNodeDrawPoint(distx, disty, virtPos);
                g.setColor(nodeMainColour);
                g.fillOval((int) (drawPos.getX() - radius), (int) (drawPos.getY() - radius), radius * 2, radius * 2);
                g.setColor(nodeOutlineColour);
                g.drawOval((int) (drawPos.getX() - radius), (int) (drawPos.getY() - radius), radius * 2, radius * 2);
            }
        }
    }
    
    private Point getNodeDrawPoint(double distx, double disty, Point virtPos) {
        Point drawPos = new Point();
        drawPos.x = (int) ((distx * virtPos.x) + (distx/2));
        drawPos.y = (int) ((disty * virtPos.y) + (disty/2));
        return drawPos;
    }
    
    public void initializeNetwork() {
        Node root = new Node(internet);
        root.enterNetwork(new Point(0,0), root.getNodeID(), root.getNodeID());
    }
    
    public void performAction(ScheduledAction action) {
        switch(action.getAction()) {
        case nodejoin:
            Node newNode = new Node(internet);
            newNode.attemptJoin(action.getNodeParam());
        }
    }
    
    public void tick() {
        Node.tickAll();
        internet.tick();
        this.drawArea.render();
    }
}
