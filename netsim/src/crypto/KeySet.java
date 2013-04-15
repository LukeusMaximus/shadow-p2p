package crypto;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KeySet {
    private UUID nodeID;
    private PseudoKeyPair dataKey;
    private PseudoKeyPair signKey;
    private Map<Integer, PseudoKeyPair> routeKeys;
    private byte[] proofOfWork;
    private byte[] signature;
    
    public KeySet(UUID nodeID, PseudoKeyPair dataKey, PseudoKeyPair signKey,
            Map<Integer, PseudoKeyPair> routeKeys, byte[] proofOfWork,
            byte[] signature) {
        super();
        this.nodeID = nodeID;
        this.dataKey = dataKey;
        this.signKey = signKey;
        this.routeKeys = routeKeys;
        this.proofOfWork = proofOfWork;
        this.signature = signature;
    }

    public PseudoKeyPair getDataKey() {
        return dataKey;
    }

    public PseudoKeyPair getSignKey() {
        return signKey;
    }

    public PseudoKeyPair getRouteKey(Integer dimension) {
        return routeKeys.get(dimension);
    }
    
    public Map<Integer, PseudoPrivateKey> getPrivateRouteKeys() {
        Map<Integer, PseudoPrivateKey> keys = new HashMap<Integer, PseudoPrivateKey>();
        for(Integer d : routeKeys.keySet()) {
            keys.put(d, routeKeys.get(d).getPrivateKey());
        }
        return keys;
    }

    public byte[] getProofOfWork() {
        return proofOfWork;
    }

    public byte[] getSignature() {
        return signature;
    }
    
    public PublicKeySet getPublic() {
        Map<Integer, PseudoPublicKey> pubRouteKeys = new HashMap<Integer, PseudoPublicKey>();
        for(Integer dimension : routeKeys.keySet()) {
            pubRouteKeys.put(dimension, routeKeys.get(dimension).getPublicKey());
        }
        PublicKeySet pub = new PublicKeySet(nodeID, dataKey.getPublicKey(), 
                signKey.getPublicKey(), pubRouteKeys, proofOfWork, signature);
        return pub;
    }

    public static KeySet generateKeySet(UUID nodeID) {
        PseudoKeyPair dataKey = new PseudoKeyPair();
        PseudoKeyPair signKey = new PseudoKeyPair();
        Map<Integer, PseudoKeyPair> routeKeys = new HashMap<Integer, PseudoKeyPair>();
        for(int i = 0; i < 2; i++) {
            PseudoKeyPair routeKey = new PseudoKeyPair();
            routeKeys.put(i, routeKey);
        }
        byte[] pow = "This is a proof of work".getBytes();
        byte[] signature = "This is a signature".getBytes();
        
        return new KeySet(nodeID, dataKey, signKey, routeKeys, pow, signature);
    }
}
