package crypto;

import java.util.Map;
import java.util.UUID;

public class PublicKeySet {
    private UUID nodeID;
    private PseudoPublicKey dataKey;
    private PseudoPublicKey signKey;
    private Map<Integer, PseudoPublicKey> routeKeys;
    private byte[] proofOfWork;
    private byte[] signature;
    
    public PublicKeySet(UUID nodeID, PseudoPublicKey dataKey, PseudoPublicKey signKey,
            Map<Integer, PseudoPublicKey> routeKeys, byte[] proofOfWork,
            byte[] signature) {
        super();
        this.nodeID = nodeID;
        this.dataKey = dataKey;
        this.signKey = signKey;
        this.routeKeys = routeKeys;
        this.proofOfWork = proofOfWork;
        this.signature = signature;
    }
    
    public UUID getNodeID() {
        return nodeID;
    }
    public PseudoPublicKey getDataKey() {
        return dataKey;
    }
    public PseudoPublicKey getSignKey() {
        return signKey;
    }
    public Map<Integer, PseudoPublicKey> getRouteKeys() {
        return routeKeys;
    }
    public PseudoPublicKey getRouteKey(Integer direction) {
        return routeKeys.get(direction);
    }
    public byte[] getProofOfWork() {
        return proofOfWork;
    }
    public byte[] getSignature() {
        return signature;
    }
}
