package virtualposition;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import crypto.PseudoPrivateKey;
import crypto.PseudoPublicKey;

public class VirtualPositionMap {
    private Map<Point, VirtualPositionKnowledge> map;
    private UUID nodeID;
    private Point localRoot;
    private Integer level;
    private Integer networkWidth;
    private Random random;
    
    public VirtualPositionMap(UUID nodeID, Point localRoot, VirtualPositionCertificate v) {
        this.map = new HashMap<Point, VirtualPositionKnowledge>();
        this.nodeID = nodeID;
        this.localRoot = localRoot;
        this.level = 1;
        this.networkWidth = 1;
        this.random = new Random();
        while(this.networkWidth <= localRoot.x || this.networkWidth <= localRoot.y) {
            this.networkWidth *= 2;
            this.level *= 2;
        }
        map.put(this.localRoot, new VirtualPositionKnowledge(this.nodeID, this.localRoot, v));
    }
    
    public Point getAvailableChildOnEdge() {
        if(this.localRoot.x < this.networkWidth / 2 && this.localRoot.y < this.networkWidth / 2) {
            // Has this node given away its positions in the outermost expansion
            Point p = new Point(localRoot);
            p.x += this.networkWidth / 2;
            if(map.get(p).nodeID.equals(this.nodeID)) return p;
            p.y += this.networkWidth / 2;
            if(map.get(p).nodeID.equals(this.nodeID)) return p;
            p.x -= this.networkWidth / 2;
            if(map.get(p).nodeID.equals(this.nodeID)) return p;
        }
        return null;
    }
    
    public UUID findJoinerNode() {
        // Are there any spaces at the outermost expansion for a new node
        if(this.networkWidth == 1) return null;
        Point p = new Point(0, 0);
        for(p.x = 0; p.x < this.networkWidth; ++p.x) {
            for(p.y = 0; p.y < this.networkWidth; ++p.y) {
                if(p.x >= this.networkWidth / 2 || p.y >= this.networkWidth / 2) {
                    UUID a = map.get(p).nodeID;
                    UUID b = map.get(VirtualPositionMap.getParent(p)).nodeID;
                    if(a != null && b != null && a.equals(b)) return a;
                }
            }
        }
        // If not then this node is the joiner node
        return null;
    }
    
    public void expandNetwork() {
        int step = this.networkWidth;
        Point p = new Point(0, 0);
        for(p.x = 0; p.x < step; ++p.x) {
            for(p.y = 0; p.y < step; ++p.y) {
                VirtualPositionKnowledge v = map.get(p);
                if(v != null) {
                    Point p2 = new Point(p.x + step, p.y);
                    v = new VirtualPositionKnowledge(v, p2);
                    map.put(p2, v);
                    p2 = new Point(p.x, p.y + step);
                    v = new VirtualPositionKnowledge(v, p2);
                    map.put(p2, v);
                    p2 = new Point(p.x + step, p.y + step);
                    v = new VirtualPositionKnowledge(v, p2);
                    map.put(p2, v);
                }
            }
        }
        this.networkWidth *= 2;
    }
    
    public Integer getNetworkWidth() {
        return this.networkWidth;
    }

    public boolean ownsPosition(Point testPoint) {
        Point p = this.wrapToNetworkWidth(testPoint);
        VirtualPositionKnowledge v = map.get(p);
        if(v == null) return false;
        return v.nodeID.equals(this.nodeID);
    }
    
    public Collection<Point> listPointsOwned() {
        Collection<Point> points = new HashSet<Point>();
        points.add(this.localRoot);
        for(Point p : map.keySet()) {
            VirtualPositionKnowledge v = map.get(p);
            if(v.nodeID.equals(this.nodeID)) {
                points.add(p);
            }
        }
        return points;
    }
    
    public Collection<Point> listPointsOwnedByNode(UUID nodeID) {
        Collection<Point> points = new HashSet<Point>();
        points.add(this.localRoot);
        for(Point p : map.keySet()) {
            VirtualPositionKnowledge v = map.get(p);
            if(v.nodeID.equals(nodeID)) {
                points.add(p);
            }
        }
        return points;
    }

    public Point getLocalRoot() {
        return localRoot;
    }
    
    public Point getNorthPosition(Point pos) {
        Point p = new Point(pos);
        p.y = (p.y + 1) % networkWidth;
        return p;
    }
    
    public Point getEastPosition(Point pos) {
        Point p = new Point(pos);
        p.x = (p.x + 1) % networkWidth;
        return p;
    }
    
    public Point getSouthPosition(Point pos) {
        Point p = new Point(pos);
        p.y = (p.y - 1) % networkWidth;
        return p;
    }
    
    public Point getWestPosition(Point pos) {
        Point p = new Point(pos);
        p.x = (p.x - 1) % networkWidth;
        return p;
    }
    
    public UUID getNodeIDFromPosition(Point p) {
        Point p2 = this.wrapToNetworkWidth(p);
        VirtualPositionKnowledge v = map.get(p2);
        if(v == null) return null;
        return v.nodeID;
    }
    
    public void verifiedOwnershipChange(PseudoPublicKey k, VirtualPositionCertificate v) {
        System.out.println("\t\tverifiedOwnershipChange");
        if(!v.isValid(k)) return;
        System.out.println("\t\t" + v.toString());
        VirtualPositionKnowledge vpk = new VirtualPositionKnowledge(v.getReceiver(), v.getPosition(), v);
        if(v.getPosition().x == 0 && v.getPosition().y == 0) {
            VirtualPositionKnowledge vRoot = map.get(new Point(0, 0));
            if(vRoot == null || vRoot.nodeID.equals(v.getGiver())) {
                map.put(v.getPosition(), vpk);
                checkChildrenIntegrity(v);
            }
        } else {
            VirtualPositionKnowledge vp = map.get(getParent(v.getPosition()));
            VirtualPositionKnowledge vr = map.get(v.getPosition());
            if(vr == null) {
                if(vp == null) {
                    map.put(v.getPosition(), vpk);
                    checkChildrenIntegrity(v);
                } else {
                    System.out.println("\t\tvp " + vp.toString());
                    if(vp.nodeID.equals(v.getGiver())) {
                        map.put(v.getPosition(), vpk);
                        checkChildrenIntegrity(v);
                    }
                }
            } else {
                System.out.println("\t\tvr " + vr.toString());
                if(vp != null) {
                    System.out.println("\t\tvp " + vp.toString());
                    if(vp.nodeID.equals(v.getGiver()) && vp.nodeID.equals(vr.nodeID)) {
                        map.put(v.getPosition(), vpk);
                        checkChildrenIntegrity(v);
                    } else if(vp.nodeID.equals(v.getReceiver())) {
                        // This is a revocation, the certificate reverts to that of the parent
                        map.put(v.getPosition(), new VirtualPositionKnowledge(vp, v.getPosition()));
                        checkChildrenIntegrity(v);
                    }
                }
            }
        }
    }
    
    private void checkChildrenIntegrity(VirtualPositionCertificate v) {
        // check children's integrity if they exist
        VirtualPositionKnowledge parentKnowledge =  map.get(v.getPosition());
        VirtualPositionKnowledge childKnowledge = map.get(getChildX(v.getPosition()));
        if(childKnowledge != null && !childKnowledge.cert.getGiver().equals(parentKnowledge.nodeID)) {
            map.remove(childKnowledge.position);
        }
        childKnowledge = map.get(getChildXY(v.getPosition()));
        if(childKnowledge != null && !childKnowledge.cert.getGiver().equals(parentKnowledge.nodeID)) {
            map.remove(childKnowledge.position);
        }
        childKnowledge = map.get(getChildY(v.getPosition()));
        if(childKnowledge != null && !childKnowledge.cert.getGiver().equals(parentKnowledge.nodeID)) {
            map.remove(childKnowledge.position);
        }
    }
    
    public VirtualPositionCertificate makeCertForPositionOwnershipChange(UUID receiver, Point p, PseudoPrivateKey k) {
        if(map.get(p).nodeID.equals(this.nodeID)) {
            return new VirtualPositionCertificate(this.nodeID, receiver, p, k);
        }
        return null;
    }
    
    public boolean hasCompleteNetworkKnowledge() {
        Point p = new Point(0, 0);
        for(p.x = 0; p.x < this.networkWidth; ++p.x) {
            for(p.y = 0; p.y < this.networkWidth; ++p.y) {
                if(map.get(p) == null) return false;
            }
        }
        return true;
    }
    
    public UUID getNextNodeIDOnRoute(UUID finalDestination) {
        Collection<Point> myPoints = this.listPointsOwned();
        Collection<Point> theirPoints = this.listPointsOwnedByNode(finalDestination);
        Double best = Double.POSITIVE_INFINITY;
        UUID bestID = null;
        for(Point p : myPoints) {
            for(Point q : theirPoints) {
                int dist = Math.abs(p.x - q.x) + Math.abs(p.y - q.y);
                if(dist < best) {
                    best = (double)dist;
                    bestID = this.map.get(q).nodeID;
                }
            }
        }
        return bestID;
    }
    
    public Set<UUID> getDownstreamNeighbours() {
        Set<UUID> dsns = new HashSet<UUID>();
        for(Point p : map.keySet()) {
            if(this.nodeID.equals(this.getNodeIDFromPosition(p))) {
                Point p2 = this.getEastPosition(p);
                UUID id = this.getNodeIDFromPosition(p2);
                if(id != null && !this.nodeID.equals(id)) dsns.add(id);
                p2 = this.getNorthPosition(p);
                id = this.getNodeIDFromPosition(p2);
                if(id != null && !this.nodeID.equals(id)) dsns.add(id);
            }
        }
        return dsns;
    }
    
    public Set<VirtualPositionCertificate> getAllCertificates() {
        Set<VirtualPositionCertificate> certs = new HashSet<VirtualPositionCertificate>();
        for(Point p : map.keySet()) {
            VirtualPositionKnowledge vpk = map.get(p);
            if(vpk != null) {
                certs.add(vpk.cert);
            }
        }
        return certs;
    }
    
    public boolean hasCertificate(VirtualPositionCertificate v) {
        for(VirtualPositionKnowledge info : map.values()) {
            if(info.cert.equals(v)) return true;
        }
        return false;
    }
    
    public Set<UUID> getNodesInDirection(Integer direction) {
        Collection<Point> points = listPointsOwned();
        Set<UUID> addresses = new HashSet<UUID>();
        for(Point p : points) {
            Point next;
            if(direction == 0) {
                next = getEastPosition(p);
            } else {
                next = getNorthPosition(p);
            }
            UUID addr = getNodeIDFromPosition(next);
            if(!addr.equals(this.nodeID)) {
                addresses.add(addr);
            }
        }
        return addresses;
    }
    
    public VirtPosPath makePath(UUID destination) {
        Point p = new Point(0,0);
        Point result = null;
        for(p.x = 0; p.x < this.networkWidth; ++p.x) {
            for(p.y = 0; p.y < this.networkWidth; ++p.y) {
                if(map.get(p).nodeID.equals(destination)) {
                    result = new Point(p);
                }
            }
        }
        Collection<Point> ownPoints = listPointsOwned();
        Point best = new Point(0,0);
        double bestDist = Double.POSITIVE_INFINITY;
        for(Point o : ownPoints) {
            double dist = Math.sqrt(Math.pow(result.x - o.x, 2) + Math.pow(result.y - o.y, 2));
            if(dist < bestDist) {
                bestDist = dist;
                best = o;
            }
        }
        List<UUID> addresses = new ArrayList<UUID>();
        List<Integer> directions = new ArrayList<Integer>();
        while(!best.equals(result)) {
            Integer d = this.random.nextInt(2);
            if(d == 0) {
                ++best.x;
            } else {
                ++best.y;
            }
            directions.add(d);
            addresses.add(getNodeIDFromPosition(best));
        }
        VirtPosPath path = new VirtPosPath();
        path.addresses = addresses;
        path.directions = directions;
        return path;
    }
    
    public static Point getParent(Point p) {
        Point result = new Point(p);
        if(p.x == 0 && p.y == 0) {
            return result;
        }
        int step = 1;
        while(p.x >= step || p.y >= step) {
            step *= 2;
        }
        step /= 2;
        if(p.x >= step) {
            result.x -= step;
        }
        if(p.y >= step) {
            result.y -= step;
        }
        return result;
    }
    
    public static Point getChildX(Point p) {
        return new Point(p.x + calculateStepFromPoint(p), p.y);
    }
    public static Point getChildXY(Point p) {
        return new Point(p.x + calculateStepFromPoint(p), p.y + calculateStepFromPoint(p));
    }
    public static Point getChildY(Point p) {
        return new Point(p.x, p.y + calculateStepFromPoint(p));
    }
    
    private static int calculateStepFromPoint(Point p) {
        int step = 1;
        while(p.x >= step || p.y >= step) {
            step *= 2;
        }
        step /= 2;
        return step;
    }
    
    private Point wrapToNetworkWidth(Point pos) {
        Point p = new Point(pos);
        p.x = p.x % this.networkWidth;
        p.y = p.y % this.networkWidth;
        return p;
    }
    
    private class VirtualPositionKnowledge {
        public Point position;
        public UUID nodeID;
        public VirtualPositionCertificate cert;
        
        public VirtualPositionKnowledge(VirtualPositionKnowledge v) {
            this.position = new Point(v.position);
            this.nodeID = v.nodeID;
            this.cert = v.cert;
        }
        
        public VirtualPositionKnowledge(VirtualPositionKnowledge v, Point p) {
            this.position = new Point(p);
            this.nodeID = v.nodeID;
            this.cert = v.cert;
        }

        public VirtualPositionKnowledge(UUID n, Point p, VirtualPositionCertificate v) {
            this.nodeID = n;
            this.position = p;
            this.cert = v;
        }

        @Override
        public String toString() {
            return "VirtualPositionKnowledge [position=" + position
                    + ", nodeID=" + nodeID + "]";
        }
        
    }

    public class VirtPosPath {
        public List<UUID> addresses;
        public List<Integer> directions;
    }
}
