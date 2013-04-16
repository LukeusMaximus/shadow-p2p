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

import packets.BalanceRequest;
import packets.BalanceResponse;
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
    private Queue<Packet> unjoinedBuffer;
    
    private VirtualPositionMap virtPosMap;
    private Map<UUID, PublicKeySet> keyRing; 
    private Integer balanceCounter; 
    private boolean balancing;
        
    public Node(PacketShepard internet) {
        this.nodeID = UUID.randomUUID();
        commonSetup(internet);
    }    
    
    public Node(PacketShepard internet, UUID uuid) {
        this.nodeID = uuid;
        commonSetup(internet);
    }
    
    private void commonSetup(PacketShepard internet) {
        this.inBuffer = new LinkedList<Packet>();
        this.unjoinedBuffer = new LinkedList<Packet>();
        this.virtPosMap = null;
        this.internet = internet;
        this.inNetwork = false;
        long col = this.nodeID.getLeastSignificantBits();
        this.nodeColour = new Color((int)col & 255, (int)(col >> 8) & 255, (int)(col >> 16) & 255);
        this.keySet = KeySet.generateKeySet(this.nodeID);
        this.keyRing = new HashMap<UUID, PublicKeySet>();
        this.balanceCounter = 0;
        this.balancing = false;
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
        // If appropriate, check balance
        if(!balancing && incBalance()) {
            if(virtPosMap != null) {
                Point p = virtPosMap.performBalanceCheck();
                if(p != null) {
                    System.out.println("Possible balance point " + p.toString());
                    UUID id = virtPosMap.getNodeIDFromPosition(p);
                    balancing = true;
                    performBalance(id, p);
                }
            }
        }
    }

    private void performBalance(UUID id, Point p) {
        BalanceRequest bal = new BalanceRequest(id, nodeID, VirtualPositionMap.getLevel(p));
        GenericData genData = makeGenericDataPacket(id, "<Encapsulated>");
        genData.setEncapsulation(bal);
        internet.sendPacket(genData);
    }

    private boolean incBalance() {
        ++this.balanceCounter;
        if(this.balanceCounter >= 40) {
            this.balanceCounter = 0;
            return true;
        }
        return false;   
    }

    public void attemptContraction() {
        if(virtPosMap != null) virtPosMap.attemptNetworkContraction();
    }
    
    public void enterNetwork(Point pos, VirtualPositionCertificate v) {
        System.out.println("Entering network at " + pos.toString());
        this.inNetwork = true;
        this.virtPosMap = new VirtualPositionMap(this.nodeID, pos, v);
        inBuffer.addAll(unjoinedBuffer);
    }
    
    public void makeRootNode() {
        System.out.println("Making this node the root node.");
        this.inNetwork = true;
        VirtualPositionCertificate v = new VirtualPositionCertificate(nodeID, nodeID,
                new Point(0, 0), keySet.getSignKey().getPrivateKey());
        this.virtPosMap = new VirtualPositionMap(this.nodeID, new Point(0, 0), v);
        this.keyRing = new HashMap<UUID, PublicKeySet>();
    }
    
    private PseudoPublicKey[] makeRouteRoutingKeys(List<Integer> dirs, List<UUID> addrs) {
        Integer[] directions = dirs.toArray(new Integer[0]);
        UUID[] addresses = addrs.toArray(new UUID[0]);
        List<PseudoPublicKey> keys = new ArrayList<PseudoPublicKey>();
        for(int i = 0; i < directions.length; ++i) {
            PublicKeySet keyset = keyRing.get(addresses[i]);
            if(keyset == null) {
                return null;
            }
            PseudoPublicKey key = keyset.getRouteKey(directions[i]);
            if(key == null) {
                return null;
            }
            keys.add(key);
        }
        return keys.toArray(new PseudoPublicKey[0]);
    }
    
    private GenericData makeGenericDataPacket(UUID destination, String data) {
        PublicKeySet joinerKeySet = keyRing.get(destination);
        // Get the route
        VirtualPositionMap.VirtPosPath path = virtPosMap.makePath(destination);
        List<UUID> addresses = path.addresses;
        List<Integer> directions = path.directions;
        
        for(int i = 0; i < addresses.size(); ++i) {
            System.out.println(addresses.get(i) + " " + directions.get(i));
        }
        
        // Extract first address
        addresses.remove(0);
        directions.remove(0);
        UUID addr1 = addresses.get(0);
        // make routing keys if needed
        PseudoPublicKey[] routeKeys = null;
        if(addresses.size() >= 1) {
            routeKeys = makeRouteRoutingKeys(directions, addresses);
        }
        GenericData gd = new GenericData(addr1, routeKeys,
                joinerKeySet.getDataKey(), data);
        return gd;
    }
    
    private void processPacket(Packet packet) {
        if(packet.getClass() == GenericData.class) {
            System.out.println("Generic data received.");
            GenericData genData = (GenericData)packet;
            if(genData.canDecrypt(this.keySet.getDataKey().getPrivateKey())) {
                System.out.println("\tAttempting decryption");
                genData.decrypt(this.keySet.getDataKey().getPrivateKey());
                if(genData.getData() == "<Encapsulated>") {
                    processPacket(genData.getEncapsulation());
                } else {
                    System.out.println("\tdata = " + genData.getData());
                }
                
            } else {
                Integer direction = genData.getNextDirection(this.keySet.getPrivateRouteKeys());
                if(direction != null) {
                    System.out.println("\tRouting successfully");
                    genData.scramble();
                    Set<UUID> nextIDs = virtPosMap.getNodesInDirection(direction);
                    Collection<GenericData> packets = genData.reAddress(nextIDs);
                    for(Packet p : packets) {
                        System.out.println("\tSending packet");
                        internet.sendPacket(p);
                    }
                } else {
                    System.out.println("\tNo route to continue");
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
                
                boolean useJoiner = false;
                if(pos == null) {
                    UUID joiner = virtPosMap.findJoinerNode();
                    if(joiner != null) {
                        System.out.println("\tUsing a joiner node with ID " + joiner.toString());
                        useJoiner = true;
                        PublicKeySet joinerKeySet = keyRing.get(joiner);
                        if(joinerKeySet != null) {
                            GenericData gd = makeGenericDataPacket(joiner, "<Encapsulated>");
                            gd.setEncapsulation(req);
                            internet.sendPacket(gd);
                            System.out.println("\tSent to joiner");
                        }
                    } else {
                        System.out.println("\tIncreasing network size");
                        // Increase network size
                        virtPosMap.expandNetwork();
                    }
                } else {
                    System.out.println("\tGiving owned position");
                }
                if(!useJoiner) {
                 // Get available position and perform ownership changes 
                    pos = virtPosMap.getAvailableChildOnEdge();
                    VirtualPositionCertificate vCert = virtPosMap.makeCertForPositionOwnershipChange(req.getNodeID(),
                            pos, keySet.getSignKey().getPrivateKey());
                    virtPosMap.verifiedOwnershipChange(vCert);
                    // Send response
                    JoinResponse resp = new JoinResponse(req.getNodeID(), pos.x, pos.y, vCert);
                    internet.sendPacket(resp);
                    // Broadcast joining node's keyset and virtual position certificate
                    for(UUID dest : this.virtPosMap.getDownstreamNeighbours()) {
                        KeySetInfoBroadcast ksib = new KeySetInfoBroadcast(dest, req.getKeySet());
                        internet.sendPacket(ksib);
                        VirtualPositionInfoBroadcast vpib = new VirtualPositionInfoBroadcast(dest, vCert);
                        internet.sendPacket(vpib);
                    }
                    
                    // Send node keysets to joining node
                    //  Send own keyset
                    KeySetInfoDirect keySetPacket = new KeySetInfoDirect(req.getNodeID(), req.getNodeID(), keySet.getPublic());
                    internet.sendPacket(keySetPacket);
                    for(UUID id : keyRing.keySet()) {
                        keySetPacket = new KeySetInfoDirect(req.getNodeID(), req.getNodeID(), keyRing.get(id));
                        internet.sendPacket(keySetPacket);
                    }
                    // Send virtual position certificates 
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
            if(virtPosMap == null) {
                this.unjoinedBuffer.add(packet);
            } else {
                VirtualPositionInfoDirect vpid = (VirtualPositionInfoDirect)packet;
                if(this.virtPosMap.getLocalRoot().equals(new Point(0, 1))) {
                    System.out.println("Received certificate for " + vpid.getvCert().getPosition());
                }
                PublicKeySet giverKeySet = keyRing.get(vpid.getvCert().getGiver());
                if(giverKeySet != null) {
                    if(!vpid.getvCert().isValid(giverKeySet.getSignKey())) {
                        System.out.println("\tcertificate not valid");
                    }
                }
                virtPosMap.verifiedOwnershipChange(vpid.getvCert());
                if(!this.nodeID.equals(vpid.getFinalDestination())) {
                    UUID nextNodeID = virtPosMap.getNextNodeIDOnRoute(vpid.getFinalDestination());
                    if(nextNodeID != null) {
                        VirtualPositionInfoDirect vpid2 = new VirtualPositionInfoDirect(nextNodeID, vpid.getFinalDestination(), vpid.getvCert());
                        internet.sendPacket(vpid2);
                    }
                }
            }
        } else if(packet.getClass() == VirtualPositionInfoBroadcast.class) {
            if(virtPosMap == null) {
                this.unjoinedBuffer.add(packet);
            } else {
                VirtualPositionInfoBroadcast vpib = (VirtualPositionInfoBroadcast)packet;
                PublicKeySet giverKeySet = keyRing.get(vpib.getvCert().getGiver());
                if(giverKeySet != null) {
                    if(!vpib.getvCert().isValid(giverKeySet.getSignKey())) {
                        System.out.println("\tcertificate not valid");
                    }
                }
                if(!virtPosMap.hasCertificate(vpib.getvCert())) {
                    virtPosMap.verifiedOwnershipChange(vpib.getvCert());
                    for(UUID dest : this.virtPosMap.getDownstreamNeighbours()) {
                        VirtualPositionInfoBroadcast vpib2 = new VirtualPositionInfoBroadcast(dest, vpib.getvCert());
                        internet.sendPacket(vpib2);
                    }
                }
            }
        } else if(packet.getClass() == BalanceRequest.class) {
            System.out.println("BalanceRequest received.");
            if(virtPosMap != null) {
                BalanceRequest req = (BalanceRequest)packet;
                Point p = virtPosMap.getAvailableChild();
                Integer level = VirtualPositionMap.getLevel(p);
                System.out.println("\tposition " + p.toString() + " level " + level + " desired level " + req.getLevel());
                if(level <= req.getLevel()) {
                    VirtualPositionCertificate vCert = virtPosMap.makeCertForPositionOwnershipChange(req.getNodeID(),
                            p, keySet.getSignKey().getPrivateKey());
                    BalanceResponse resp = new BalanceResponse(req.nodeID, vCert);
                    GenericData gd = makeGenericDataPacket(req.nodeID, "<Encapsulated>");
                    gd.setEncapsulation(resp);
                    internet.sendPacket(gd);
                    virtPosMap.verifiedOwnershipChange(vCert);
                }
            }
        } else if(packet.getClass() == BalanceResponse.class) {
            System.out.println("BalanceResponse received.");
            if(virtPosMap != null) {
                BalanceResponse resp = (BalanceResponse)packet;
                VirtualPositionCertificate newCert = resp.getNewPosCert();
                Point p = newCert.getPosition();
                System.out.println("\tOwn level " + virtPosMap.getOwnLevel() + " packet level " + VirtualPositionMap.getLevel(p));
                if(VirtualPositionMap.getLevel(p) < virtPosMap.getOwnLevel()) {
                    VirtualPositionCertificate returnCert = virtPosMap.makeCertForReturn(this.keySet.getSignKey().getPrivateKey());
                    virtPosMap.verifiedOwnershipChange(returnCert);
                    virtPosMap.verifiedOwnershipChange(newCert);
                    for(UUID dest : this.virtPosMap.getDownstreamNeighbours()) {
                        VirtualPositionInfoBroadcast vpib = new VirtualPositionInfoBroadcast(dest, returnCert);
                        internet.sendPacket(vpib);
                        vpib = new VirtualPositionInfoBroadcast(dest, newCert);
                        internet.sendPacket(vpib);
                    }
                    virtPosMap.changeLocalRoot(resp.getNewPosCert().getPosition());
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
    
    public void leaveNetwork() {
        if(virtPosMap != null) {
            VirtualPositionCertificate returnCert = virtPosMap.makeCertForReturn(this.keySet.getSignKey().getPrivateKey());
            for(UUID dest : this.virtPosMap.getDownstreamNeighbours()) {
                VirtualPositionInfoBroadcast vpib = new VirtualPositionInfoBroadcast(dest, returnCert);
                internet.sendPacket(vpib);
            }
        }
        nodes.remove(this);
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
