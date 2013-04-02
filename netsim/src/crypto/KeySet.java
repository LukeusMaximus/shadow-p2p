package crypto;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KeySet {
    private UUID nodeID;
    private KeyPair dataKey;
    private KeyPair signKey;
    private Map<Integer, KeyPair> routeKeys;
    private byte[] proofOfWork;
    private byte[] signature;
    
    public KeySet(UUID nodeID, KeyPair dataKey, KeyPair signKey,
            Map<Integer, KeyPair> routeKeys, byte[] proofOfWork,
            byte[] signature) {
        super();
        this.nodeID = nodeID;
        this.dataKey = dataKey;
        this.signKey = signKey;
        this.routeKeys = routeKeys;
        this.proofOfWork = proofOfWork;
        this.signature = signature;
    }

    public KeyPair getDataKey() {
        return dataKey;
    }

    public KeyPair getSignKey() {
        return signKey;
    }

    public KeyPair getRouteKey(Integer dimension) {
        return routeKeys.get(dimension);
    }

    public byte[] getProofOfWork() {
        return proofOfWork;
    }

    public byte[] getSignature() {
        return signature;
    }
    
    public PublicKeySet getPublic() {
        Map<Integer, PublicKey> pubRouteKeys = new HashMap<Integer, PublicKey>();
        for(Integer dimension : routeKeys.keySet()) {
            pubRouteKeys.put(dimension, routeKeys.get(dimension).getPublic());
        }
        PublicKeySet pub = new PublicKeySet(nodeID, dataKey.getPublic(), 
                signKey.getPublic(), pubRouteKeys, proofOfWork, signature);
        return pub;
    }

    public static KeySet generateKeySet(UUID nodeID) {
        SecureRandom random = new SecureRandom();
        
        KeyPairGenerator elGamalGenerator = null;
        try {
            elGamalGenerator = KeyPairGenerator.getInstance("ElGamal", "BC");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            return null;
        }
        elGamalGenerator.initialize(128, random);
        
        KeyPairGenerator rsaGenerator = null;
        try {
            rsaGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            return null;
        }
        elGamalGenerator.initialize(128, random);

        KeyPair dataKey = elGamalGenerator.generateKeyPair();
        KeyPair signKey = rsaGenerator.generateKeyPair();
        Map<Integer, KeyPair> routeKeys = new HashMap<Integer, KeyPair>();
        for(int i = 0; i < 2; ++i) {
            routeKeys.put(i, elGamalGenerator.generateKeyPair());
        }
        byte[] pow = new byte[5];
        for(int i = 0; i < pow.length; ++i) {
            pow[i] = (byte)i;
        }
        
        Signature signer;
        String plain = "blah";
        byte[] signature = null;
        try {
            signer = Signature.getInstance("SHA1withRSA");
            signer.initSign(signKey.getPrivate());       
            signer.update(plain.getBytes());
            signature = signer.sign();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (SignatureException e) {
            e.printStackTrace();
            return null;
        }
        
        return new KeySet(nodeID, dataKey, signKey, routeKeys, pow, signature);
    }
}
