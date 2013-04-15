package packets;

import java.awt.Point;
import java.util.UUID;

public class Packet {
    private UUID destination;
    private Integer ticks;
    
    private Point startPosition;
    private Point endPosition;
    
    public Packet(UUID destination) {
        this.destination = destination;
        this.ticks = 0;
        this.startPosition = null;
        this.endPosition = null;
    }
    
    public Point getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(Point startVirtualPosition) {
        this.startPosition = startVirtualPosition;
    }

    public Point getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(Point endVirtualPosition) {
        this.endPosition = endVirtualPosition;
    }

    public Integer getTicks() {
        return ticks;
    }
    
    public void tick() {
        ++this.ticks;
    }

    public UUID getDestination() {
        return destination;
    }
    
}
