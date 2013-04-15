package packets;

import java.util.UUID;

import virtualposition.VirtualPositionCertificate;


public class JoinResponse extends Packet {
    private int x;
    private int y;
    private VirtualPositionCertificate ownPosCert;
    
    public JoinResponse(UUID dest, int x, int y, VirtualPositionCertificate ownPosCert) {
        super(dest);
        this.x = x;
        this.y = y;
        this.ownPosCert = ownPosCert;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public VirtualPositionCertificate getOwnPosCert() {
        return ownPosCert;
    }

}
