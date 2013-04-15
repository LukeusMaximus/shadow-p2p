package virtualposition;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VirtualPositionMap2 {

    
    private class VPTreeNode {
        private VPTreeNode parent;
        private Map<Integer, VPTreeNode> childrenX;
        private Map<Integer, VPTreeNode> childrenXY;
        private Map<Integer, VPTreeNode> childrenY;
        private Point pos;
        private UUID nodeID;
        private VirtualPositionCertificate cert;
        private Integer minStep;
        
        public VPTreeNode(UUID nodeID, VirtualPositionCertificate cert) {
            this.parent = this;
            this.childrenX = new HashMap<Integer, VPTreeNode>();
            this.childrenXY = new HashMap<Integer, VPTreeNode>();
            this.childrenY = new HashMap<Integer, VPTreeNode>();
            this.pos = new Point(0,0);
            this.nodeID = nodeID;
            this.cert = cert;
            this.minStep = 1;
        }
        
        private VPTreeNode(UUID nodeID, VPTreeNode parent, Point pos, VirtualPositionCertificate cert, Integer minStep) {
            this.parent = parent;
            this.childrenX = new HashMap<Integer, VPTreeNode>();
            this.childrenXY = new HashMap<Integer, VPTreeNode>();
            this.childrenY = new HashMap<Integer, VPTreeNode>();
            this.pos = pos;
            this.nodeID = nodeID;
            this.cert = cert;
            this.minStep = minStep;
        }
        
        public VPTreeNode getParent() {
            return parent;
        }

        public Point getPos() {
            return pos;
        }

        public UUID getNodeID() {
            return nodeID;
        }

        public VirtualPositionCertificate getCert() {
            return cert;
        }
        
//        public VPTreeNode makeChildX(UUID nodeID, VirtualPositionCertificate cert) {
//            childX = new VPTreeNode(nodeID, this, cert.getPosition(), cert, minStep * 2)
//            return ;
//        }
        
    }
}
