package components;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import packets.DummyPacket;
import packets.JoinRequest;
import packets.JoinResponse;
import packets.Packet;
import virtualposition.VirtualPositionMap;
import crypto.KeySet;

public class Node {
    
    public static ArrayList<Node> nodes = new ArrayList<Node>();
    
    private PacketShepard internet;
    
    private UUID nodeID;
    private Color nodeColour;
    private Boolean inNetwork;
    private Queue<Packet> inBuffer;
    private VirtualPositionMap virtPosMap;
    private KeySet keySet;
    
    public Node(PacketShepard internet) {
        this.nodeID = UUID.randomUUID();
        this.inBuffer = new LinkedList<Packet>();
        this.virtPosMap = null;
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
        return virtPosMap.getNetworkWidth();
    }
    
    public boolean ownsPosition(Point nodePos) {
        return virtPosMap.ownsPosition(nodePos);
    }
    
    public Collection<Point> getPositionsResponsible() {
        return virtPosMap.listPointsOwned();
    }

    public void addMessageToInputBuffer(Packet packet) {
        inBuffer.add(packet);
    }
    
    public void tick() {
        // Process everything in inBuffer.
        while(!inBuffer.isEmpty()) {
            processPacket(inBuffer.poll());
        }
    }
    
    public void attemptJoin(Point virtualLocationParam) {
        // Find joiner node
        System.out.println("Attempting to join network at " + virtualLocationParam.toString());
        Node foundNode = Node.findNodeByPoint(virtualLocationParam);
        
        // Add join message to their input buffer
        JoinRequest req = new JoinRequest(foundNode.getNodeID(), this.nodeID);
        internet.sendPacket(req);
    }
    
    public void sendDummyPacket() {
        Collection<Point> points = virtPosMap.listPointsOwned();
        for(Point p : points) {
            Point eastPos = virtPosMap.getEastPosition(p);
            DummyPacket packet = new DummyPacket(virtPosMap.getNodeIDFromPosition(eastPos));
            packet.setStartPosition(p);
            packet.setEndPosition(eastPos);
            internet.sendPacket(packet);
            
            Point northPos = virtPosMap.getNorthPosition(p);
            packet = new DummyPacket(virtPosMap.getNodeIDFromPosition(northPos));
            packet.setStartPosition(p);
            packet.setEndPosition(northPos);
            internet.sendPacket(packet);
        }
    }
    
    public void enterNetwork(Point pos, UUID north, UUID east) {
        System.out.println("Entering network at " + pos.toString());
        this.inNetwork = true;
        this.virtPosMap = new VirtualPositionMap(this.nodeID, pos);
    }
    
    private void processPacket(Packet packet) {
        if(packet.getClass() == JoinRequest.class) {
            System.out.println("Request received.");
            JoinRequest req = (JoinRequest)packet;
            
            // Find appropriate position for the new node to join
            Point pos = virtPosMap.getAvailableChildOnEdge();
            
            if(pos != null) {
                System.out.println(pos.x + " " + pos.y);
                // Send response
                JoinResponse resp = new JoinResponse(req.getNodeID(), pos.x, pos.y,
                        virtPosMap.getNodeIDFromPosition(virtPosMap.getNorthPosition(pos)),
                        virtPosMap.getNodeIDFromPosition(virtPosMap.getEastPosition(pos)));
                internet.sendPacket(resp);
            } else {
                System.out.println("pos is null");
                UUID joiner = virtPosMap.findJoinerNode();
                if(joiner != null) {
                    System.out.println("joiner found with ID " + joiner.toString());
                    // Send join request encapsulated to joiner
                    
                } else {
                    System.out.println("increasing network size");
                    // Increase network size
                    virtPosMap.expandNetwork();
                    // Get available position and perform ownership changes 
                    pos = virtPosMap.getAvailableChildOnEdge();
                    virtPosMap.changeOwnership(pos, req.getNodeID());
                    // TODO certificate of ownership transfer
                    // Send response
                    JoinResponse resp = new JoinResponse(req.getNodeID(), pos.x, pos.y,
                            virtPosMap.getNodeIDFromPosition(virtPosMap.getNorthPosition(pos)),
                            virtPosMap.getNodeIDFromPosition(virtPosMap.getEastPosition(pos)));
                    internet.sendPacket(resp);
                }
            }
        } else if(packet.getClass() == JoinResponse.class) {
            System.out.println("Response received.");
            if(!this.inNetwork) {
                System.out.println("Response being processed.");
                JoinResponse resp = (JoinResponse)packet;
                Point joinPos = new Point(resp.getX(), resp.getY());
                enterNetwork(joinPos, resp.getNorth(), resp.getEast());
            }
        }
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
    
}
