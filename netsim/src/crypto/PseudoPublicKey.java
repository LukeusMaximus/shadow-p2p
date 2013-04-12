package crypto;

import java.util.UUID;

public class PseudoPublicKey {
    private UUID publicKey;
    
    public PseudoPublicKey(UUID publicKey) {
        this.publicKey = publicKey;
    }

    public UUID getPublicKey() {
        return publicKey;
    }

    
}
