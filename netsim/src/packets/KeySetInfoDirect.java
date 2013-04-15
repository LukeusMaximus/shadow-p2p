package packets;

import java.util.UUID;

import crypto.PublicKeySet;

public class KeySetInfoDirect extends Packet {
    private PublicKeySet keySet;
    private UUID finalDestination;
    
    public KeySetInfoDirect(UUID destination, UUID finalDestination, PublicKeySet keySet) {
        super(destination);
        this.keySet = keySet;
        this.finalDestination = finalDestination;
    }

    public UUID getFinalDestination() {
        return finalDestination;
    }

    public PublicKeySet getKeySet() {
        return this.keySet;
    }
}
