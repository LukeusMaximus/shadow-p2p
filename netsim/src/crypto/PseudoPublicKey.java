package crypto;

import java.util.UUID;

public class PseudoPublicKey {
    private UUID privateKey;
    private UUID publicKey;
    
    public PseudoPublicKey(UUID publicKey, UUID privateKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public UUID getPublicKey() {
        return publicKey;
    }

    public UUID getVerificationKey() {
        return this.privateKey;
    }
}
