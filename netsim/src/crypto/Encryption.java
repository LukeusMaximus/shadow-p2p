package crypto;

import java.util.Stack;
import java.util.UUID;

public class Encryption {
    private String data;
    public Stack<EncryptionInfo> encryptions;
    
    public Encryption(String data) {
        this.data = data;
        this.encryptions = new Stack<EncryptionInfo>();
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
        encryptions.push(new EncryptionInfo(k.getPublicKey(), "EncA"));
    }
    
    public void DecryptAsymmetric(PseudoPrivateKey k) {
        if(!encryptions.isEmpty()) {
            EncryptionInfo ei = encryptions.peek();
            if(ei.method.equals("EncA") && k.canDecrypt(ei.key)) {
                encryptions.pop();
            }
        }
    }
    
    public void Sign(PseudoPrivateKey k) {
        encryptions.push(new EncryptionInfo(k.getPrivateKey(), "Sign"));
    }
    
    public boolean Verify(PseudoPublicKey k, String s) {
        if(!encryptions.isEmpty()) {
            EncryptionInfo ei = encryptions.peek();
            if(ei.method.equals("Sign") && k.getVerificationKey().equals(ei.key) && data.equals(s)) {
                return true;
            }
        }
        return false;
    }
    
    public void EncryptSymmetric(UUID k) {
        encryptions.push(new EncryptionInfo(k, "EncS"));
    }
    
    public void DecryptSymmetric(UUID k) {
        if(!encryptions.isEmpty()) {
            EncryptionInfo ei = encryptions.peek();
            if(ei.method.equals("EncS") && k.equals(ei.key)) {
                encryptions.pop();
            }
        }
    }
    
    private class EncryptionInfo {
        public UUID key;
        public String method;
        
        public EncryptionInfo(UUID k, String s) {
            this.key = k;
            this.method = s;
        }
    }
}
