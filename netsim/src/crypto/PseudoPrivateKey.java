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
        return new PseudoPublicKey(publicKey);
    }

    public boolean canDecrypt(UUID k) {
        return k.equals(publicKey);
    }
}
