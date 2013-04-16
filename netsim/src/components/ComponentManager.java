package components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private List<UUID> idList;
    
    public ComponentManager() {
        this.internet = new PacketShepard();
        this.idList = new ArrayList<UUID>();
//        this.idList.add(UUID.fromString("e5b64c6e-386e-4808-b114-c911a8a0a8f8"));
//        this.idList.add(UUID.fromString("d5d83681-0737-4bfb-94b4-9782e71c8d84"));
//        this.idList.add(UUID.fromString("1cf3cdcc-f326-4fcb-82ef-829399cc058b"));
//        this.idList.add(UUID.fromString("946be0ac-a04a-4519-99fd-9865b3fe7ab7"));
//        this.idList.add(UUID.fromString("46ec836e-1b21-4e76-b6df-f4aa48302746"));
//        this.idList.add(UUID.fromString("85c887f2-3ba2-4417-99f4-e1b138c3fc17"));
//        this.idList.add(UUID.fromString("fd7b5a12-88c8-416d-9e02-e65dbb85ec93"));
//        this.idList.add(UUID.fromString("f71bea93-1930-429b-8ade-6b48224699a2"));
        this.idList.add(UUID.fromString("4c6ee5b6-386e-4808-b114-a0a8f8c911a8"));
        this.idList.add(UUID.fromString("3681d5d8-0737-4bfb-94b4-1c8d849782e7"));
        this.idList.add(UUID.fromString("cdcc1cf3-f326-4fcb-82ef-cc058b829399"));
        this.idList.add(UUID.fromString("e0ac946b-a04a-4519-99fd-fe7ab79865b3"));
        this.idList.add(UUID.fromString("836e46ec-1b21-4e76-b6df-302746f4aa48"));
        this.idList.add(UUID.fromString("87f285c8-3ba2-4417-99f4-c3fc17e1b138"));
        this.idList.add(UUID.fromString("5a12fd7b-88c8-416d-9e02-85ec93e65dbb"));
        this.idList.add(UUID.fromString("ea93f71b-1930-429b-8ade-4699a26b4822"));
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
                drawPos2.y -= disty/4;
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
                g.drawString(node.getNodeID().toString().substring(0,4), drawPos.x + nodeRadius, drawPos.y - nodeRadius);
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
        UUID id = this.idList.remove(0);
        Node root = new Node(internet, id);
        root.makeRootNode();
    }
    
    public void performAction(ScheduledAction action) {
        switch(action.getAction()) {
        case nodejoin:
            UUID id = this.idList.remove(0);
            Node newNode = new Node(internet, id);
            System.out.println("New node " + newNode.getNodeID().toString());
            newNode.attemptJoin(action.getNodeParam());
            break;
        case nodeleave:
            Node leavingNode = Node.findNodeByPoint(action.getNodeParam());
            System.out.println("Leaving node " + leavingNode.getNodeID().toString());
            leavingNode.leaveNetwork();
            break;
        case attemptContraction:
            for(Node n : Node.getNodesInNetwork()) {
                n.attemptContraction();
            }
            break;           
        case sendDummy:
            for(Node node : Node.getNodesInNetwork()) {
                node.sendDummyPacket();
            }
            break;
        }
    }
    
    public void tick() {
        Node.tickAll();
        internet.tick();
        this.drawArea.render();
    }
}
