package packets;

import java.util.UUID;

import crypto.PublicKeySet;

public class JoinRequest extends Packet {
    private UUID nodeID;
    private PublicKeySet keySet;

    public JoinRequest(UUID dest, UUID nodeID, PublicKeySet keySet) {
        super(dest);
        this.nodeID = nodeID;
        this.keySet = keySet;
    }

    public UUID getNodeID() {
        return nodeID;
    }

    public PublicKeySet getKeySet() {
        return keySet;
    }

}
