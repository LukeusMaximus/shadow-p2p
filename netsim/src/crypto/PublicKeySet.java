package crypto;

import java.security.PublicKey;
import java.util.Map;
import java.util.UUID;

public class PublicKeySet {
    private UUID nodeID;
    private PublicKey dataKey;
    private PublicKey signKey;
    private Map<Integer, PublicKey> routeKeys;
    private byte[] proofOfWork;
    private byte[] signature;
    
    public PublicKeySet(UUID nodeID, PublicKey dataKey, PublicKey signKey,
            Map<Integer, PublicKey> routeKeys, byte[] proofOfWork,
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
    public PublicKey getDataKey() {
        return dataKey;
    }
    public PublicKey getSignKey() {
        return signKey;
    }
    public Map<Integer, PublicKey> getRouteKeys() {
        return routeKeys;
    }
    public byte[] getProofOfWork() {
        return proofOfWork;
    }
    public byte[] getSignature() {
        return signature;
    }
}
