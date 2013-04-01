package components;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class PacketShepard {
    private static final Integer travelTime = 20;
    private Collection<Packet> packets;
    
    public PacketShepard() {
        packets = new HashSet<Packet>();
    }
    
    public void tick() {
        Collection<Packet> removeSet = new HashSet<Packet>();
        for(Packet packet : packets) {
            packet.tick();
            if(packet.getTicks() > travelTime) {
                removeSet.add(packet);
            }
        }
        for(Packet packet : removeSet) {
            Node.findNodeByUUID(packet.destination).addMessageToInputBuffer(packet.message);
            packets.remove(packet);
        }
    }
    
    public void sendPacket(UUID destination, String message) {
        Packet packet = new Packet(destination, message);
        packets.add(packet);
    }
    
    private class Packet {
        private UUID destination;
        private String message;
        private Integer ticks;
        
        Packet(UUID destination, String message) {
            this.destination = destination;
            this.message = message;
            this.ticks = 0;
        }

        public String getMessage() {
            return message;
        }

        public Integer getTicks() {
            return ticks;
        }
        
        public void tick() {
            ++this.ticks;
        }
    }
}
