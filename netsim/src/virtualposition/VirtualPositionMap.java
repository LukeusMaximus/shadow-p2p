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

public class VirtualPositionMap {
    private Map<Point, VirtualPositionKnowledge> map;
    private UUID nodeID;
    private Point localRoot;
    private Integer level;
    private Integer networkWidth;
    private Random random;
    private Set<VirtualPositionCertificate> returnCerts;
    
    private boolean debug;
    
    public VirtualPositionMap(UUID nodeID, Point localRoot, VirtualPositionCertificate v) {
        this.map = new HashMap<Point, VirtualPositionKnowledge>();
        this.returnCerts = new HashSet<VirtualPositionCertificate>();
        this.nodeID = nodeID;
        this.localRoot = localRoot;
        this.level = 1;
        this.networkWidth = 1;
        this.debug = false;
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
    
    public Point getAvailableChild() {
        int step = this.level;
        if(step != 1) step /= 2;
        for(; step < this.networkWidth / 2; step *= 2) {
            Point p = new Point(localRoot);
            p.x += step;
            if(map.get(p).nodeID.equals(this.nodeID)) return p;
            p.y += step;
            if(map.get(p).nodeID.equals(this.nodeID)) return p;
            p.x -= step;
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
                    UUID b = map.get(getParent(p)).nodeID;
                    if(a != null && b != null && a.equals(b)) return b;
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
                    map.put(p2, new VirtualPositionKnowledge(v, p2));
                    p2 = new Point(p.x, p.y + step);
                    map.put(p2, new VirtualPositionKnowledge(v, p2));
                    p2 = new Point(p.x + step, p.y + step);
                    map.put(p2, new VirtualPositionKnowledge(v, p2));
                }
            }
        }
        this.networkWidth *= 2;
    }
    
    public Integer getNetworkWidth() {
        return this.networkWidth;
    }

    public Integer getOwnLevel() {
        return this.level;
    }
    
    public void setDebug(boolean value) {
        this.debug = value;
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
            if(debug) System.out.println(p);
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
    
    public void verifiedOwnershipChange(VirtualPositionCertificate v) {
        if(v.getPosition().x == 0 && v.getPosition().y == 0) {
            VirtualPositionKnowledge vRoot = map.get(new Point(0, 0));
            if(vRoot == null || vRoot.nodeID.equals(v.getGiver())) {
                setPointAndDescendantsToCertificate(v, v);
            }
        } else {
            if(v.getPosition().x >= networkWidth || v.getPosition().y >= networkWidth) {
                expandNetwork();
            }
            VirtualPositionKnowledge vp = map.get(getParent(v.getPosition()));
            VirtualPositionKnowledge vr = map.get(v.getPosition());
            if(vr == null) {
                if(vp == null) {
                    setPointAndDescendantsToCertificate(v, v);
                } else {
                    if(vp.nodeID.equals(v.getGiver())) {
                        setPointAndDescendantsToCertificate(v, v);
                    }
                }
            } else {
                if(vp != null) {
                    if(vp.nodeID.equals(v.getGiver()) && vp.nodeID.equals(vr.nodeID)) {
                        setPointAndDescendantsToCertificate(v, v);
                    } else if(vp.nodeID.equals(v.getReceiver())) {
                        // This is a revocation, the certificate reverts to that of the parent
                        returnCerts.add(v);
                        setPointAndDescendantsToCertificate(v, vp.cert);
                    }
                }
            }
        }
    }
    
    private void setPointAndDescendantsToCertificate(VirtualPositionCertificate changeCert, VirtualPositionCertificate setCert) {
        if(debug) System.out.println(changeCert.toString());
        VirtualPositionKnowledge vpk;
        vpk = map.get(changeCert.getPosition());
        if(vpk == null || vpk.nodeID.equals(changeCert.getGiver())) {
            if(debug) System.out.println("change " + changeCert.getPosition().toString());
            map.put(new Point(changeCert.getPosition()),
                    new VirtualPositionKnowledge(changeCert.getReceiver(), new Point(changeCert.getPosition()), setCert));
            if(debug) System.out.println(map.get(changeCert.getPosition()));
        }
        for(int step = getLevel(changeCert.getPosition()); step < this.networkWidth; step *= 2) {
            if(debug) System.out.println("step " + step);
            Point p = new Point(changeCert.getPosition());
            p.x += step;
            vpk = map.get(p);
            if(vpk == null || vpk.nodeID.equals(changeCert.getGiver())) {
                if(debug) System.out.println("change " + p);
                map.put(new Point(p), new VirtualPositionKnowledge(changeCert.getReceiver(), new Point(p), setCert));
                if(debug) System.out.println(map.get(p));
            }
            p.y += step;
            vpk = map.get(p);
            if(vpk == null || vpk.nodeID.equals(changeCert.getGiver())) {
                if(debug) System.out.println("change " + p);
                map.put(new Point(p), new VirtualPositionKnowledge(changeCert.getReceiver(), new Point(p), setCert));
                if(debug) System.out.println(map.get(p));
            }
            p.x -= step;
            vpk = map.get(p);
            if(vpk == null || vpk.nodeID.equals(changeCert.getGiver())) {
                if(debug) System.out.println("change " + p);
                map.put(new Point(p), new VirtualPositionKnowledge(changeCert.getReceiver(), new Point(p), setCert));
                if(debug) System.out.println(map.get(p));
            }
        }
    }
    
    public VirtualPositionCertificate makeCertForPositionOwnershipChange(UUID receiver, Point p, PseudoPrivateKey k) {
        if(map.get(p).nodeID.equals(this.nodeID)) {
            return new VirtualPositionCertificate(this.nodeID, receiver, p, k);
        }
        return null;
    }
    
    public VirtualPositionCertificate makeCertForReturn(PseudoPrivateKey k) {
        Point p = getParent(localRoot);
        VirtualPositionKnowledge vpk = map.get(p);
        return new VirtualPositionCertificate(this.nodeID, vpk.nodeID, localRoot, k);
    }
    
    public void attemptNetworkContraction() {
        // Network contraction will only occur if it is possible for it to do so
        int step = this.networkWidth / 2;
        Point tracer = new Point(0, 0);
        Point tracerE = new Point(0, 0);
        Point tracerNE = new Point(0, 0);
        Point tracerN = new Point(0, 0);
        boolean canContract = true;
        for(tracer.x = 0, tracerE.x = 0, tracerNE.x = 0, tracerN.x = 0;
                tracer.x < step; ++tracer.x, ++tracerE.x, ++tracerNE.x, ++tracerN.x) {
            for(tracer.y = 0, tracerE.y = 0, tracerNE.y = 0, tracerN.y = 0;
                    tracer.y < step; ++tracer.y, ++tracerE.y, ++tracerNE.y, ++tracerN.y) {
                VirtualPositionKnowledge vpk = map.get(tracer);
                VirtualPositionKnowledge vpkE = map.get(tracerE);
                VirtualPositionKnowledge vpkNE = map.get(tracerNE);
                VirtualPositionKnowledge vpkN = map.get(tracerN);
                if(vpk == null || vpkE == null || vpkNE == null || vpkN == null) {
                    canContract = false;
                } else {
                    if(!vpk.nodeID.equals(vpkE.nodeID)) canContract = false;
                    if(!vpk.nodeID.equals(vpkNE.nodeID)) canContract = false;
                    if(!vpk.nodeID.equals(vpkN.nodeID)) canContract = false;
                }
            }
        }
        if(canContract) {
            networkWidth /= 2;
            Set<Point> pointsToBeRemoved = new HashSet<Point>();
            for(Point p : map.keySet()) {
                if(p.x >= networkWidth || p.y >= networkWidth) pointsToBeRemoved.add(p);
            }
            for(Point p : pointsToBeRemoved) {
                map.remove(p);
            }
        }
    }
    
    public boolean hasCompleteNetworkKnowledge() {
        Point p = new Point(0, 0);
        System.out.println("\t" + map.size() + " positions known");
        System.out.println("\tlocal root " + localRoot.toString());
        boolean complete = true;
        for(p.x = 0; p.x < this.networkWidth; ++p.x) {
            for(p.y = 0; p.y < this.networkWidth; ++p.y) {
                if(map.get(p) == null) {
                    complete = false;
                    System.out.println("\tUnknown what is at " + p.toString());
                }
            }
        }
        return complete;
    }
    
    public Integer getAmountOfKnowledge() {
        Point p = new Point(0, 0);
        Integer amount = 0;
        for(p.x = 0; p.x < this.networkWidth; ++p.x) {
            for(p.y = 0; p.y < this.networkWidth; ++p.y) {
                if(map.get(p) != null) {
                    ++amount;
                }
            }
        }
        return amount;
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
        for(VirtualPositionKnowledge vpk : map.values()) {
            certs.add(vpk.cert);
        }
        return certs;
    }
    
    public boolean hasCertificate(VirtualPositionCertificate v) {
        for(VirtualPositionKnowledge info : map.values()) {
            if(info.cert.equals(v)) return true;
        }
        for(VirtualPositionCertificate vCert : returnCerts) {
            if(vCert.equals(v)) return true;
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
            if(addr != null && !this.nodeID.equals(addr)) {
                addresses.add(addr);
            }
        }
        return addresses;
    }
    
    public VirtPosPath makePath(UUID destination) {
        List<Point> route = new ArrayList<Point>();
        Map<Point, Point> possibles = new HashMap<Point, Point>();
        Map<Point, Point> good = new HashMap<Point, Point>();
        
        possibles.put(getLocalRoot(), getLocalRoot());
        boolean done = false;
        while(!done && possibles.size() > 0) {
            Map<Point, Point> poss = new HashMap<Point, Point>();
            poss.putAll(possibles);
            for(Point p : poss.keySet()) {
                if(map.get(p) != null) {
                    good.put(p, poss.get(p));
                    if(map.get(p).nodeID.equals(destination)) {
                        done = true;
                        route.add(p);
                    } else {
                        Point p2 = getNorthPosition(p);
                        if(!good.containsKey(p2)) possibles.put(p2, p);
                        p2 = getEastPosition(p);
                        if(!good.containsKey(p2)) possibles.put(p2, p);
                    }
                }
                possibles.remove(p);
                if(done) break;
            }
        }
        
        List<UUID> addresses = new ArrayList<UUID>();
        List<Integer> directions = new ArrayList<Integer>();
        
        if(!done) {
            System.out.println("No route found");
            addresses.add(this.nodeID);
            directions.add(0);
            addresses.add(destination);
            directions.add(0);
            VirtPosPath path = new VirtPosPath();
            path.addresses = addresses;
            path.directions = directions;
            return path;
        }
        
        System.out.println("do reassembly");
        Point search = route.get(0);
        while(!search.equals(localRoot)) {
            search = good.get(search);
            route.add(0, search);
        }
        for(Point p : route) System.out.println("route " + p.toString());
        
        Point[] routePoints = route.toArray(new Point[0]);
        for(int i = 0; i < routePoints.length - 1; ++i) {
            VirtualPositionKnowledge vpk = map.get(routePoints[i]);
            addresses.add(vpk.nodeID);
            if(routePoints[i].x != routePoints[i+1].x) {
                directions.add(0);
            } else {
                directions.add(1);
            }
        }
        VirtualPositionKnowledge vpk = map.get(routePoints[routePoints.length-1]);
        addresses.add(vpk.nodeID);
        directions.add(0);
        
        VirtPosPath path = new VirtPosPath();
        path.addresses = addresses;
        path.directions = directions;
        return path;
    }
    
    public Point performBalanceCheck() {
        // return the virtual position at which we'd rather be
        if(this.localRoot.equals(new Point(0,0))) return null;
        int step = this.level / 2;
        if(step == 1) return null;
        Point p = new Point(0, 0);
        for(p.x = 0; p.x < step; ++p.x) {
            for(p.y = 0; p.y < step; ++p.y) {
                if(p.x >= step / 2 || p.y >= step / 2) {
                    VirtualPositionKnowledge vpk = map.get(p);
                    VirtualPositionKnowledge vpkParent = map.get(getParent(p));
                    if(vpk.nodeID.equals(vpkParent.nodeID)) {
                        // We've found a better node
                        return p;
                    }
                }
            }
        }
        return null;
    }
    
    public void changeLocalRoot(Point p) {
        this.localRoot = p;
    }
    
    private Point wrapToNetworkWidth(Point pos) {
        Point p = new Point(pos);
        p.x = p.x % this.networkWidth;
        p.y = p.y % this.networkWidth;
        return p;
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
    
    public static Integer getLevel(Point p) {
        int level = 1;
        while(p.x >= level || p.y >= level) {
            level *= 2;
        }
        return level;
    }
    
    private class VirtualPositionKnowledge {
        public Point position;
        public UUID nodeID;
        public VirtualPositionCertificate cert;
        
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
