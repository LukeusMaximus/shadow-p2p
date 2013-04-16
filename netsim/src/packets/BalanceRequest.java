package packets;

import java.util.UUID;

public class BalanceRequest extends Packet {
    public UUID nodeID;
    public Integer level;

    public BalanceRequest(UUID destination, UUID nodeID, Integer level) {
        super(destination);
        this.nodeID = nodeID;
        this.level = level;
    }

    public UUID getNodeID() {
        return nodeID;
    }

    public Integer getLevel() {
        return level;
    }

}
