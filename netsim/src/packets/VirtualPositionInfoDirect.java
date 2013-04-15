package packets;

import java.util.UUID;

import virtualposition.VirtualPositionCertificate;

public class VirtualPositionInfoDirect extends Packet {
    private VirtualPositionCertificate vCert;
    private UUID finalDestination;
    
    public VirtualPositionInfoDirect(UUID destination, UUID finalDestination, VirtualPositionCertificate v) {
        super(destination);
        this.vCert = v;
        this.finalDestination = finalDestination;
    }

    public VirtualPositionCertificate getvCert() {
        return vCert;
    }

    public UUID getFinalDestination() {
        return finalDestination;
    }
    
}
