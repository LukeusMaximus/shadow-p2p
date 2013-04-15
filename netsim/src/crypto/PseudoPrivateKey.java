package crypto;

import java.util.UUID;

public class PseudoPrivateKey {
    private UUID privateKey;
    private UUID publicKey;
    
    public PseudoPrivateKey() {
        privateKey = UUID.randomUUID();
        publicKey = UUID.randomUUID();
    }
    
    public PseudoPublicKey getPseudoPublicKey() {
        return new PseudoPublicKey(publicKey, privateKey);
    }

    public boolean canDecrypt(UUID k) {
        return k.equals(publicKey);
    }
    
    public UUID getPrivateKey() {
        return this.privateKey;
    }
}
