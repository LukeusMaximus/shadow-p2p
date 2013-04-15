package packets;

import java.util.UUID;

import virtualposition.VirtualPositionCertificate;

public class VirtualPositionInfoBroadcast extends Packet {
    private VirtualPositionCertificate vCert;
    
    public VirtualPositionInfoBroadcast(UUID destination, VirtualPositionCertificate v) {
        super(destination);
        this.vCert = v;
    }

    public VirtualPositionCertificate getvCert() {
        return vCert;
    }
}
