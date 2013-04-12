package components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import packets.Packet;
import simulation.ScheduledAction;
import ui.DrawArea;


public class ComponentManager {
    
    private static final Integer nodeRadius = 10;
    private static final Integer packetRadius = 3;
    private static final Color nodeOutlineColour = new Color(0, 0, 0);
    private static final Color linkColour = new Color(0, 0, 0);
    private static final Color packetColor = new Color(0, 128, 0);
    
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
        Point drawPos, drawPos2, virtPos;
        int i, j;
        Map<Point, Point> screenPositions = new HashMap<Point, Point>();
        
        for(i = 0; i < networkWidth; ++i) {
            for(j = 0; j < networkWidth; ++j) {
                virtPos = new Point(i,j);
                screenPositions.put(new Point(i,j), getNodeDrawPoint(distx, disty, bounds.height, virtPos));
            }
        }
        
        // Horizontal
        for(j = 0; j < networkWidth; ++j) {
            for(i = 0; i < networkWidth-1; ++i) {
                virtPos = new Point(i, j);
                drawPos = screenPositions.get(virtPos);
                ++virtPos.x;
                drawPos2 = screenPositions.get(virtPos);
                g.setColor(linkColour);
                g.drawLine(drawPos.x, drawPos.y, drawPos2.x, drawPos2.y);
            }
            // Horizontal trailing
            virtPos = new Point(networkWidth-1, j);
            drawPos = screenPositions.get(virtPos);
            drawPos2 = new Point(drawPos);
            drawPos2.x += distx/4;
            g.setColor(linkColour);
            g.drawLine(drawPos.x, drawPos.y, drawPos2.x, drawPos2.y);
        }
        
        // Vertical
        for(i = 0; i < networkWidth; ++i) {
            for(j = 0; j < networkWidth-1; ++j) {
                virtPos = new Point(i, j);
                drawPos = screenPositions.get(virtPos);
                ++virtPos.y;
                drawPos2 = screenPositions.get(virtPos);
                g.setColor(linkColour);
                g.drawLine(drawPos.x, drawPos.y, drawPos2.x, drawPos2.y);
            }
            // Vertical trailing
            virtPos = new Point(i, networkWidth-1);
            drawPos = screenPositions.get(virtPos);
            drawPos2 = new Point(drawPos);
            drawPos2.y -= disty/4;
            g.setColor(linkColour);
            g.drawLine(drawPos.x, drawPos.y, drawPos2.x, drawPos2.y);
        }
        
        // Packets
        for(Packet packet : internet.getNetworkPackets()) {
            double ratio = (double)packet.getTicks() / (double)PacketShepard.getTraveltime();
            drawPos = screenPositions.get(packet.getStartPosition());
            if(packet.getStartPosition().x == networkWidth-1 && packet.getEndPosition().x != packet.getStartPosition().x) {
                drawPos2 = new Point(drawPos);
                drawPos2.x += distx/4;
            } else if(packet.getStartPosition().y == networkWidth-1 && packet.getEndPosition().y != packet.getStartPosition().y) {
                drawPos2 = new Point(drawPos);
                drawPos2.y -= distx/4;
            } else {
                drawPos2 = screenPositions.get(packet.getEndPosition());
            }
            Integer drawX = drawPos.x;
            Integer drawY = drawPos.y;
            if(drawPos.x != drawPos2.x) {
                double diff = drawPos2.x - drawPos.x;
                drawX = (int)(diff * ratio) + drawPos.x; 
            }
            if(drawPos.y != drawPos2.y) {
                double diff = drawPos2.y - drawPos.y;
                drawY = (int)(diff * ratio) + drawPos.y; 
            }
            // Not forgetting to draw the actual packet
            g.setColor(packetColor);
            g.fillRect(drawX - packetRadius, drawY - packetRadius, packetRadius * 2, packetRadius * 2);
        }
        
        // Nodes
        for(Node node : Node.getNodesInNetwork()) {
            for(Point nodePos : node.getPositionsResponsible()) {
                drawPos = screenPositions.get(nodePos);
                g.setColor(node.getNodeColour());
                g.fillOval(drawPos.x - nodeRadius, drawPos.y - nodeRadius, nodeRadius * 2, nodeRadius * 2);
                g.setColor(nodeOutlineColour);
                g.drawOval(drawPos.x - nodeRadius, drawPos.y - nodeRadius, nodeRadius * 2, nodeRadius * 2);
            }
        }
    }
    
    private Point getNodeDrawPoint(double distx, double disty, int height, Point virtPos) {
        Point drawPos = new Point();
        drawPos.x = (int) ((distx * virtPos.x) + (distx/2));
        drawPos.y = (int) (height - ((disty * virtPos.y) + (disty/2)));
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
        case sendDummy:
            for(Node node : Node.getNodesInNetwork()) {
                node.sendDummyPacket();
            }
        }
    }
    
    public void tick() {
        Node.tickAll();
        internet.tick();
        this.drawArea.render();
    }
}
