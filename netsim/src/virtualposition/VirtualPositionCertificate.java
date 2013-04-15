package virtualposition;

import java.awt.Point;
import java.util.UUID;

import crypto.Encryption;
import crypto.PseudoPrivateKey;
import crypto.PseudoPublicKey;

public class VirtualPositionCertificate {
    private UUID giver;
    private UUID receiver;
    private Point position;
    private Encryption signature;
    
    public VirtualPositionCertificate(UUID giver, UUID receiver, Point position, PseudoPrivateKey k) {
        this.giver = giver;
        this.receiver = receiver;
        this.position = position;
        this.signature = new Encryption(giver.toString() + " " + receiver.toString() + " " + position.toString());
        this.signature.Sign(k);
    }
    
    public boolean isValid(PseudoPublicKey k) {
        String s = this.giver.toString() + " " + this.receiver.toString() + " " + this.position.toString();
        return signature.Verify(k, s);
    }

    public UUID getGiver() {
        return giver;
    }

    public UUID getReceiver() {
        return receiver;
    }

    public Point getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "VirtualPositionCertificate [giver=" + giver + ", receiver="
                + receiver + ", position=" + position + "]";
    }
    
}
