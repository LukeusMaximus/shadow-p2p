package crypto;


public class PseudoKeyPair {
    private PseudoPrivateKey privateKey;
    private PseudoPublicKey publicKey;
    
    public PseudoKeyPair() {
        this.privateKey = new PseudoPrivateKey();
        this.publicKey = this.privateKey.getPseudoPublicKey();
    }

    public PseudoPrivateKey getPrivateKey() {
        return privateKey;
    }

    public PseudoPublicKey getPublicKey() {
        return publicKey;
    }
    
    
}
