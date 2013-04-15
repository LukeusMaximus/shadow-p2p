package packets;

import java.util.UUID;

import crypto.PublicKeySet;

public class KeySetInfoBroadcast extends Packet {
    private PublicKeySet keySet;
    
    public KeySetInfoBroadcast(UUID destination, PublicKeySet keySet) {
        super(destination);
        this.keySet = keySet;
    }

    public PublicKeySet getKeySet() {
        return this.keySet;
    }
}
