package packets;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import crypto.Encryption;
import crypto.PseudoPrivateKey;
import crypto.PseudoPublicKey;

public class GenericData extends Packet {
    private PseudoPublicKey[] routingKeys;
    private PseudoPublicKey dataKey;
    private Encryption[] hybridHeaders;
    private Encryption data;
    private Packet encapsulation;

    public GenericData(UUID dest, PseudoPublicKey[] routingKeys, PseudoPublicKey dataKey, String data) {
        super(dest);
        this.routingKeys = routingKeys;
        this.dataKey = dataKey;
        this.data = new Encryption(data);
        UUID sk = UUID.randomUUID();
        this.data.EncryptSymmetric(sk);
        this.hybridHeaders = new Encryption[routingKeys.length]; 
        this.hybridHeaders[0] = new Encryption(sk.toString());
        this.hybridHeaders[0].EncryptAsymmetric(dataKey);
        for(int i = 1; i < this.hybridHeaders.length; i++) {
            // These represent random encryptions
            this.hybridHeaders[i] = new Encryption("blah");
        }
        this.encapsulation = null;
    }
    
    private GenericData(UUID dest, PseudoPublicKey[] routingKeys, PseudoPublicKey dataKey,
            Encryption[] hybridHeaders, Encryption data, Packet encapsulation) {
        super(dest);
        this.routingKeys = routingKeys;
        this.dataKey = dataKey;
        this.hybridHeaders = hybridHeaders;
        this.data = data;
        this.encapsulation = encapsulation;
    }
    
    public void scramble() {
        UUID sk = UUID.randomUUID();
        this.data.EncryptSymmetric(sk);
        Encryption[] temp = new Encryption[this.hybridHeaders.length + 1];
        for(int i = 0; i < this.hybridHeaders.length; ++i) {
            temp[i+1] = this.hybridHeaders[i];
            temp[i+1].EncryptSymmetric(sk);
        }
        this.hybridHeaders = temp;
        this.hybridHeaders[0] = new Encryption(sk.toString());
        this.hybridHeaders[0].EncryptAsymmetric(dataKey);
        for(int i = 0; i < this.routingKeys.length-1; ++i) {
            this.routingKeys[i] = this.routingKeys[i+1];
        }
    }
    
    public Collection<GenericData> reAddress(Collection<UUID> addresses) {
        Collection<GenericData> packets = new HashSet<GenericData>();
        for(UUID address : addresses) {
            packets.add(new GenericData(address, routingKeys, dataKey, hybridHeaders, data, encapsulation));
        }
        return packets;
    }

    public boolean canDecrypt(PseudoPrivateKey k) {
        return k.canDecrypt(this.dataKey.getPublicKey());
    }
    
    public String decrypt(PseudoPrivateKey k) {
        int i = 0;
        while(!this.data.isDecrypted()) {
            this.hybridHeaders[i].DecryptAsymmetric(k);
            String key = this.hybridHeaders[i].getData();
            if(key == null) {
                return key;
            }
            UUID sk = UUID.fromString(key);
            for(int j = i+1; j < this.hybridHeaders.length; j++) {
                this.hybridHeaders[j].DecryptSymmetric(sk);
            }
            this.data.DecryptSymmetric(sk);
            ++i;
        }
        return data.getData();
    }
    
    public Integer getNextDirection(Map<Integer, PseudoPrivateKey> routingKeys) {
        if(this.routingKeys == null) return null;
        for(Integer d : routingKeys.keySet()) {
            if(routingKeys.get(d).canDecrypt(this.routingKeys[0].getPublicKey())) {
                return d;
            }
        }
        return null;
    }
    
    public String getData() {
        return data.getData();
    }

    public Packet getEncapsulation() {
        return encapsulation;
    }

    public void setEncapsulation(Packet encapsulation) {
        this.encapsulation = encapsulation;
    }
}
