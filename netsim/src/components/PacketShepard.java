package components;

import java.util.Collection;
import java.util.HashSet;

import packets.Packet;

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
            Node receiver = Node.findNodeByUUID(packet.getDestination());
            if(receiver != null) receiver.addMessageToInputBuffer(packet);
            packets.remove(packet);
        }
    }
    
    public static Integer getTraveltime() {
        return travelTime;
    }
    
    public void sendPacket(Packet packet) {
        packets.add(packet);
    }
    
    public Collection<Packet> getNetworkPackets() {
        Collection<Packet> networkPackets = new HashSet<Packet>();
        for(Packet p : packets) {
            if(p.getEndPosition() != null && p.getStartPosition() != null) {
                networkPackets.add(p);
            }
        }
        return networkPackets;
    }
    
}
