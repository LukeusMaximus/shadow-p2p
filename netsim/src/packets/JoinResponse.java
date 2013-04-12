package packets;

import java.util.UUID;


public class JoinResponse extends Packet {
    private int x;
    private int y;
    private UUID north;
    private UUID east;
    
    public JoinResponse(UUID dest, int x, int y, UUID north, UUID east) {
        super(dest);
        this.x = x;
        this.y = y;
        this.north = north;
        this.east = east;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public UUID getNorth() {
        return north;
    }

    public UUID getEast() {
        return east;
    }

}
