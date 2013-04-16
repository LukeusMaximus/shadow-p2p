package packets;

import java.util.UUID;

import virtualposition.VirtualPositionCertificate;

public class BalanceResponse extends Packet {
    private VirtualPositionCertificate newPosCert;
    
    public BalanceResponse(UUID destination, VirtualPositionCertificate newPosCert) {
        super(destination);
        this.newPosCert = newPosCert;
    }

    public VirtualPositionCertificate getNewPosCert() {
        return newPosCert;
    }

}
