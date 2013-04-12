package packets;

import java.util.UUID;

public class JoinRequest extends Packet {
    private UUID nodeID;

    public JoinRequest(UUID dest, UUID nodeID) {
        super(dest);
        this.nodeID = nodeID;
    }

    public UUID getNodeID() {
        return nodeID;
    }

}
