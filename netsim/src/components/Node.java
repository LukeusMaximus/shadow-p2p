package components;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import packets.DummyPacket;
import packets.GenericData;
import packets.JoinRequest;
import packets.JoinResponse;
import packets.KeySetInfoBroadcast;
import packets.KeySetInfoDirect;
import packets.Packet;
import packets.VirtualPositionInfoBroadcast;
import packets.VirtualPositionInfoDirect;
import virtualposition.VirtualPositionCertificate;
import virtualposition.VirtualPositionMap;
import crypto.KeySet;
import crypto.PseudoPublicKey;
import crypto.PublicKeySet;

public class Node {
    
    public static ArrayList<Node> nodes = new ArrayList<Node>();
    
    private PacketShepard internet;
    
    private UUID nodeID;
    private KeySet keySet;
    private Color nodeColour;
    private Boolean inNetwork;
    private Queue<Packet> inBuffer;
    
    private VirtualPositionMap virtPosMap;
    private Map<UUID, PublicKeySet> keyRing; 
    
    
    public Node(PacketShepard internet) {
        this.nodeID = UUID.randomUUID();
        this.inBuffer = new LinkedList<Packet>();
        this.virtPosMap = null;
        this.internet = internet;
        this.inNetwork = false;
        
        long col = this.nodeID.getLeastSignificantBits();
        this.nodeColour = new Color((int)col & 255, (int)(col >> 8) & 255, (int)(col >> 16) & 255);
        
        this.keySet = KeySet.generateKeySet(this.nodeID);
        this.keyRing = new HashMap<UUID, PublicKeySet>();
        
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
    
    public void enterNetwork(Point pos, VirtualPositionCertificate v) {
        System.out.println("Entering network at " + pos.toString());
        this.inNetwork = true;
        this.virtPosMap = new VirtualPositionMap(this.nodeID, pos, v);
    }
    
    public void makeRootNode() {
        System.out.println("Making this node the root node.");
        this.inNetwork = true;
        VirtualPositionCertificate v = new VirtualPositionCertificate(nodeID, nodeID,
                new Point(0, 0), keySet.getSignKey().getPrivateKey());
        this.virtPosMap = new VirtualPositionMap(this.nodeID, new Point(0, 0), v);
        this.keyRing = new HashMap<UUID, PublicKeySet>();
    }
    
    public PseudoPublicKey[] makeRouteRoutingKeys(List<Integer> dirs, List<UUID> addrs) {
        Integer[] directions = dirs.toArray(new Integer[0]);
        UUID[] addresses = addrs.toArray(new UUID[0]);
        List<PseudoPublicKey> keys = new ArrayList<PseudoPublicKey>();
        for(int i = 0; i < directions.length; ++i) {
            PublicKeySet keyset = keyRing.get(addresses[i]);
            if(keyset == null) return null;
            PseudoPublicKey key = keyset.getRouteKey(directions[i]);
            if(key == null) return null;
            keys.add(key);
        }
        return keys.toArray(new PseudoPublicKey[0]);
    }
    
    private void processPacket(Packet packet) {
        if(packet.getClass() == GenericData.class) {
            System.out.println("Generic data received.");
            GenericData genData = (GenericData)packet;
            if(genData.canDecrypt(this.keySet.getDataKey().getPrivateKey())) {
                genData.decrypt(this.keySet.getDataKey().getPrivateKey());
                if(genData.getData() == "<Encapsulated>") {
                    processPacket(genData.getEncapsulation());
                } else {
                    System.out.println("\tdata = " + genData.getData());
                }
                
            } else {
                Integer direction = genData.getNextDirection(this.keySet.getPrivateRouteKeys());
                Set<UUID> nextIDs = virtPosMap.getNodesInDirection(direction);
                Collection<GenericData> packets = genData.reAddress(nextIDs);
                for(Packet p : packets) {
                    internet.sendPacket(p);
                }
            }
        } else if(packet.getClass() == JoinRequest.class) {
            System.out.println("Join request received.");
            JoinRequest req = (JoinRequest)packet;
            
            // We can only respond when we have complete network knowledge
            if(virtPosMap.hasCompleteNetworkKnowledge()) {
                System.out.println("\thas complete network knowledge");
                // Store the keyset received
                this.keyRing.put(req.getNodeID(), req.getKeySet());
                
                // Find appropriate position for the new node to join
                Point pos = virtPosMap.getAvailableChildOnEdge();
                
                boolean sent = false;
                if(pos == null) {
                    UUID joiner = virtPosMap.findJoinerNode();
                    if(joiner != null) {
                        System.out.println("\tUsing a joiner node with ID " + joiner.toString());
                        sent = true;
                        PublicKeySet joinerKeySet = keyRing.get(joiner);
                        if(joinerKeySet != null) {
                            // Send join request encapsulated to joiner
                            VirtualPositionMap.VirtPosPath path = virtPosMap.makePath(joiner);
                            List<UUID> addresses = path.addresses;
                            List<Integer> directions = path.directions;
                            PseudoPublicKey[] routeKeys = makeRouteRoutingKeys(directions, addresses);
                            if(routeKeys != null) {
                                GenericData gd = new GenericData(addresses.get(0), routeKeys,
                                        joinerKeySet.getDataKey(), "<Encapsulated>");
                                internet.sendPacket(gd);
                                System.out.println("\tSent to joiner");
                            }
                        }
                    } else {
                        System.out.println("\tIncreasing network size");
                        // Increase network size
                        virtPosMap.expandNetwork();
                    }
                } else {
                    System.out.println("\tGiving owned position");
                }
                if(!sent) {
                 // Get available position and perform ownership changes 
                    pos = virtPosMap.getAvailableChildOnEdge();
                    VirtualPositionCertificate vCert = virtPosMap.makeCertForPositionOwnershipChange(req.getNodeID(),
                            pos, keySet.getSignKey().getPrivateKey());
                    virtPosMap.verifiedOwnershipChange(keySet.getSignKey().getPublicKey(), vCert);
                    // Send response
                    System.out.println("\tSending response");
                    JoinResponse resp = new JoinResponse(req.getNodeID(), pos.x, pos.y, vCert);
                    internet.sendPacket(resp);
                    // Broadcast joining node's keyset and virtual position certificate
                    System.out.println("\tSending joining node all keysets and virtual position certificates");
                    for(UUID dest : this.virtPosMap.getDownstreamNeighbours()) {
                        KeySetInfoBroadcast ksib = new KeySetInfoBroadcast(dest, req.getKeySet());
                        internet.sendPacket(ksib);
                        VirtualPositionInfoBroadcast vpib = new VirtualPositionInfoBroadcast(dest, vCert);
                        internet.sendPacket(vpib);
                    }
                    
                    // Send node keysets to joining node
                    System.out.println("\tBroadcasting joining node's keyset");
                    for(UUID id : keyRing.keySet()) {
                        KeySetInfoDirect keySetPacket = new KeySetInfoDirect(req.getNodeID(), req.getNodeID(), keyRing.get(id));
                        internet.sendPacket(keySetPacket);
                    }
                    // Send virtual position certificates 
                    System.out.println("\tBroadcasting joining node's virtual position certificate");
                    for(VirtualPositionCertificate v : virtPosMap.getAllCertificates()) {
                        VirtualPositionInfoDirect virtPosInfoPacket = new VirtualPositionInfoDirect(req.getNodeID(), req.getNodeID(), v);
                        internet.sendPacket(virtPosInfoPacket);
                    }
                }
            }
        } else if(packet.getClass() == JoinResponse.class) {
            if(!this.inNetwork) {
                System.out.println("Join response received.");
                JoinResponse resp = (JoinResponse)packet;
                Point joinPos = new Point(resp.getX(), resp.getY());
                enterNetwork(joinPos, resp.getOwnPosCert());
            }
        } else if(packet.getClass() == KeySetInfoDirect.class) {
            System.out.println("Keyset direct received.");
            KeySetInfoDirect ksid = (KeySetInfoDirect)packet;
            if(!this.keyRing.containsKey(ksid.getKeySet().getNodeID())) {
                this.keyRing.put(ksid.getKeySet().getNodeID(), ksid.getKeySet());
            }
            if(!this.nodeID.equals(ksid.getFinalDestination())) {
                UUID nextNodeID = virtPosMap.getNextNodeIDOnRoute(ksid.getFinalDestination());
                if(nextNodeID != null) {
                    KeySetInfoDirect ksid2 = new KeySetInfoDirect(nextNodeID, ksid.getFinalDestination(), ksid.getKeySet());
                    internet.sendPacket(ksid2);
                }
            }
        } else if(packet.getClass() == KeySetInfoBroadcast.class) {
            System.out.println("Keyset broadcast received.");
            KeySetInfoBroadcast ksib = (KeySetInfoBroadcast)packet;
            if(!this.keyRing.containsKey(ksib.getKeySet().getNodeID())) {
                this.keyRing.put(ksib.getKeySet().getNodeID(), ksib.getKeySet());
                if(this.virtPosMap != null) {
                    for(UUID dest : this.virtPosMap.getDownstreamNeighbours()) {
                        KeySetInfoBroadcast ksib2 = new KeySetInfoBroadcast(dest, ksib.getKeySet());
                        internet.sendPacket(ksib2);
                    }
                }
            }
        } else if(packet.getClass() == VirtualPositionInfoDirect.class) {
            System.out.println("Virtual position certificate direct received.");
            VirtualPositionInfoDirect vpid = (VirtualPositionInfoDirect)packet;
            PublicKeySet giverKeySet = keyRing.get(vpid.getvCert().getGiver());
            if(giverKeySet != null) {
                virtPosMap.verifiedOwnershipChange(giverKeySet.getSignKey(), vpid.getvCert());
            }
            if(!this.nodeID.equals(vpid.getFinalDestination())) {
                UUID nextNodeID = virtPosMap.getNextNodeIDOnRoute(vpid.getFinalDestination());
                if(nextNodeID != null) {
                    VirtualPositionInfoDirect vpid2 = new VirtualPositionInfoDirect(nextNodeID, vpid.getFinalDestination(), vpid.getvCert());
                    internet.sendPacket(vpid2);
                }
            }
        } else if(packet.getClass() == VirtualPositionInfoBroadcast.class) {
            System.out.println("Virtual position certificate broadcast received.");
            VirtualPositionInfoBroadcast vpib = (VirtualPositionInfoBroadcast)packet;
            PublicKeySet giverKeySet = keyRing.get(vpib.getvCert().getGiver());
            if(giverKeySet != null) {
                if(vpib.getvCert().isValid(giverKeySet.getSignKey())) {
                    if(!virtPosMap.hasCertificate(vpib.getvCert())) {
                        virtPosMap.verifiedOwnershipChange(giverKeySet.getSignKey(), vpib.getvCert());
                        for(UUID dest : this.virtPosMap.getDownstreamNeighbours()) {
                            VirtualPositionInfoBroadcast vpib2 = new VirtualPositionInfoBroadcast(dest, vpib.getvCert());
                            internet.sendPacket(vpib2);
                        }
                    }
                }
            }
        }
    }
    
    // Simulation related functions
    
    public void attemptJoin(Point virtualLocationParam) {
        // Find joiner node
        System.out.println("Attempting to join network at " + virtualLocationParam.toString());
        Node foundNode = Node.findNodeByPoint(virtualLocationParam);
        
        // Add join message to their input buffer
        JoinRequest req = new JoinRequest(foundNode.getNodeID(), this.nodeID, this.keySet.getPublic());
        internet.sendPacket(req);
    }
    
    public void sendDummyPacket() {
        Collection<Point> points = virtPosMap.listPointsOwned();
        for(Point p : points) {
            Point pos = virtPosMap.getEastPosition(p);
            UUID dest = virtPosMap.getNodeIDFromPosition(pos);
            if(dest != null) {
                DummyPacket packet = new DummyPacket(dest);
                packet.setStartPosition(p);
                packet.setEndPosition(pos);
                internet.sendPacket(packet);
            }
            
            pos = virtPosMap.getNorthPosition(p);
            dest = virtPosMap.getNodeIDFromPosition(pos);
            if(dest != null) {
                DummyPacket packet = new DummyPacket(dest);
                packet.setStartPosition(p);
                packet.setEndPosition(pos);
                internet.sendPacket(packet);
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
