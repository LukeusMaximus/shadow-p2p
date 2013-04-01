package components;

import java.util.Collection;
import java.util.HashSet;

public class UniDirectionalLink{
    private static final Integer linkLength = 20; // Ticks taken to traverse link
    
    private static Collection<UniDirectionalLink> links = new HashSet<UniDirectionalLink>();
    
    private Collection<Packet> packetBuffer;
    private Node start;
    private Node end;
    
    public UniDirectionalLink(Node start, Node end) {
        this.start = start;
        this.end = end;
        links.add(this);
    }
    
    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }

    public void sendMessage(String message) {
        packetBuffer.add(new Packet(message));
    }
    
    public void tick() {
        Collection<Packet> removeSet = new HashSet<Packet>();
        for(Packet packet : packetBuffer) {
            packet.tick();
            if(packet.getTicks() > linkLength) {
                removeSet.add(packet);
            }
        }
        for(Packet packet : removeSet) {
            end.addMessageToInputBuffer(packet.getMessage());
            packetBuffer.remove(packet);
        }
    }
    
    public static void tickAll() {
        for(UniDirectionalLink link : links) {
            link.tick();
        }
    }
    
    private class Packet {
        private String message;
        private Integer ticks;
        
        Packet(String message) {
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
