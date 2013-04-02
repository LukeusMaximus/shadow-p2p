package components;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import misc.Packet;

import com.google.gson.Gson;

import crypto.KeySet;

public class Node {
    
    public static ArrayList<Node> nodes = new ArrayList<Node>();
    
    private PacketShepard internet;
    
    private UUID nodeID;
    private Color nodeColour;
    private Boolean inNetwork;
    private Queue<String> inBuffer;
    private Integer networkWidth;
    private VirtualPositionInfo localRoot;
    private Map<Point, VirtualPositionInfo> pointsOwned;
    private KeySet keySet;
    
    public Node(PacketShepard internet) {
        this.nodeID = UUID.randomUUID();
        this.inBuffer = new LinkedList<String>();
        this.networkWidth = 1;
        this.localRoot = null;
        this.pointsOwned = null;
        this.internet = internet;
        this.inNetwork = false;
        
        long col = this.nodeID.getLeastSignificantBits();
        this.nodeColour = new Color((int)col & 255, (int)(col >> 8) & 255, (int)(col >> 16) & 255);
        
        this.keySet = KeySet.generateKeySet(this.nodeID);
        
        nodes.add(this);
    }    
    
    public Color getNodeColour() {
        return nodeColour;
    }

    public Boolean isInNetwork() {
        return inNetwork;
    }
    
    @Override
    public String toString() {
        return "Node [nodeID=" + nodeID + "]";
    }
    
    public UUID getNodeID() {
        return nodeID;
    }
    
    public Integer getNetworkWidth() {
        return networkWidth;
    }
    
    public boolean ownsPosition(Point nodePos) {
        return nodePos.equals(localRoot.virtualPosition) || pointsOwned.keySet().contains(nodePos);
    }
    
    public Collection<Point> getPositionsResponsible() {
        Collection<Point> points = new HashSet<Point>(this.pointsOwned.keySet());
        points.add(localRoot.virtualPosition);
        return points;
    }

    public void addMessageToInputBuffer(String message) {
        inBuffer.add(message);
    }
    
    public void tick() {
        // Process everything in inBuffer.
        while(!inBuffer.isEmpty()) {
            processMessage(inBuffer.poll());
        }
    }
    
    public void attemptJoin(Point virtualLocationParam) {
        // Find joiner node
        System.out.println("Attempting to join network at " + virtualLocationParam.toString());
        Node foundNode = Node.findNodeByPoint(virtualLocationParam);
        
        // Add join message to their input buffer
        JoinRequest req = new JoinRequest();
        req.nodeID = this.nodeID;
        Gson gson = new Gson();
        String joinMessage = "JOIN_REQ: " + gson.toJson(req);
        
        internet.sendPacket(new Packet(foundNode.nodeID, joinMessage));
    }
    
    public void sendDummyPacket() {
        Packet packet = new Packet(localRoot.east, "DUMMY PACKET");
        packet.setStartPosition(localRoot.virtualPosition);
        Point pos = new Point(localRoot.virtualPosition);
        pos = getEastPosition(pos);
        packet.setEndPosition(pos);
        internet.sendPacket(packet);
        
        packet = new Packet(localRoot.north, "DUMMY PACKET");
        packet.setStartPosition(localRoot.virtualPosition);
        pos = new Point(localRoot.virtualPosition);
        pos = getNorthPosition(pos);
        packet.setEndPosition(pos);
        internet.sendPacket(packet);
        
        for(Point position : pointsOwned.keySet()) {
            VirtualPositionInfo virtPosInfo = pointsOwned.get(position);
            
            packet = new Packet(virtPosInfo.east, "DUMMY PACKET");
            packet.setStartPosition(virtPosInfo.virtualPosition);
            pos = new Point(virtPosInfo.virtualPosition);
            pos = getEastPosition(pos);
            packet.setEndPosition(pos);
            internet.sendPacket(packet);
            
            packet = new Packet(virtPosInfo.north, "DUMMY PACKET");
            packet.setStartPosition(virtPosInfo.virtualPosition);
            pos = new Point(virtPosInfo.virtualPosition);
            pos = getNorthPosition(pos);
            packet.setEndPosition(pos);
            internet.sendPacket(packet);
        }
    }
    
    public void enterNetwork(Point pos, UUID north, UUID east) {
        System.out.println("Entering network at " + pos.toString());
        this.inNetwork = true;
        VirtualPositionInfo virtPosInfo = new VirtualPositionInfo();
        virtPosInfo.virtualPosition = pos;
        virtPosInfo.east = east;
        virtPosInfo.north = north;
        this.localRoot = virtPosInfo;
        this.networkWidth = 1;
        while(this.networkWidth <= pos.x && this.networkWidth <= pos.y) {
            this.networkWidth *= 2;
        }
        this.pointsOwned = new HashMap<Point, VirtualPositionInfo>();
    }
    
    private void processMessage(String message) {
        System.out.println(message);
        Gson gson = new Gson();
        if(message.startsWith("JOIN_REQ: ")) {
            JoinRequest req = gson.fromJson(message.substring(10), JoinRequest.class);
            if(pointsOwned.isEmpty()) {
                networkWidth = this.increaseNetworkSize();
            }
            VirtualPositionInfo position = pointsOwned.remove(pointsOwned.keySet().iterator().next());
            
            // Send response
            JoinResponse resp = new JoinResponse();
            resp.x = position.virtualPosition.x;
            resp.y = position.virtualPosition.y;
            resp.north = position.north;
            resp.east = position.east;
            String joinReply = "JOIN_RESP: " + gson.toJson(resp);
            internet.sendPacket(new Packet(req.nodeID, joinReply));
        } else if(message.startsWith("JOIN_RESP: ")) {
            System.out.println("Response received.");
            if(localRoot == null) {
                System.out.println("Response being processed.");
                JoinResponse resp = gson.fromJson(message.substring(11), JoinResponse.class); 
                Point joinPos = new Point(resp.x, resp.y);
                enterNetwork(joinPos, resp.north, resp.east);
            }
        }
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
    
    private Integer increaseNetworkSize() {
        this.networkWidth *= 2;
        Collection<Point> points = Node.getPointsInheritedForStepSize(localRoot.virtualPosition, networkWidth/2);
        for(Point pos : points) {
            VirtualPositionInfo virtPosInfo = new VirtualPositionInfo();
            virtPosInfo.virtualPosition = pos;
            virtPosInfo.east = localRoot.east;
            virtPosInfo.north = localRoot.north;
            pointsOwned.put(pos, virtPosInfo);
        }
        return this.networkWidth;
    }
    
    public static Integer getMaxWidth() {
        Integer maxWidth = 1;
        for(Node node : nodes) {
            if(node.isInNetwork() && node.getNetworkWidth() > maxWidth) {
                maxWidth = node.getNetworkWidth();
            }
        }
        return maxWidth; 
    }
    
    public static Node findNodeByPoint(Point virtualLocationParam) {
        for(Node node : nodes) {
            if(node.isInNetwork() && node.ownsPosition(virtualLocationParam)) {
                return node;
            }
        }
        return null;
    } 
    
    public static Node findNodeByUUID(UUID nodeID) {
        for(Node node : nodes) {
            if(node.getNodeID().equals(nodeID)) {
                return node;
            }
        }
        return null;
    }

    public static void tickAll() {
        for(Node node : nodes) {
            node.tick();
        }
    }
    
    public static Collection<Node> getNodesInNetwork() {
        Collection<Node> inNetwork = new HashSet<Node>();
        for(Node node : nodes) {
            if(node.isInNetwork()) {
                inNetwork.add(node);
            }
        }
        return inNetwork;
    }
    
    private static Collection<Point> getPointsInheritedForStepSize(Point pos, Integer stepSize) {
        ArrayList<Point> points = new ArrayList<Point>();
        points.add(new Point(pos.x + stepSize, pos.y));
        points.add(new Point(pos.x + stepSize, pos.y + stepSize));
        points.add(new Point(pos.x, pos.y + stepSize));
        return points;
    }
    
    private class VirtualPositionInfo {
        public Point virtualPosition;
        public UUID north;
        public UUID east;
    }
    
    private class JoinRequest {
        public UUID nodeID;
        public JoinRequest() {}
    }
    
    private class JoinResponse {
        public int x;
        public int y;
        public UUID north;
        public UUID east;
        public JoinResponse() {}
    }
    
}
