package crypto;

import java.util.Stack;
import java.util.UUID;

public class Encryption {
    private String data;
    private Stack<UUID> encryptions;
    
    public Encryption(String data) {
        this.data = data;
        this.encryptions = new Stack<UUID>();
    }
    
    public String getData() {
        if(this.encryptions.isEmpty()) {
            return this.data;
        } else {
            return null;
        }
    }
    
    public boolean isDecrypted() {
        return this.encryptions.isEmpty();
    }
    
    public void EncryptAsymmetric(PseudoPublicKey k) {
        encryptions.push(k.getPublicKey());
    }
    
    public void DecryptAsymmetric(PseudoPrivateKey k) {
        if(!encryptions.isEmpty() && k.canDecrypt(encryptions.peek())) {
            encryptions.pop();
        }
    }
    
    public void EncryptSymmetric(UUID k) {
        encryptions.push(k);
    }
    
    public void DecryptSymmetric(UUID k) {
        if(!encryptions.isEmpty() && encryptions.peek().equals(k)) {
            encryptions.pop();
        }
    }
}
