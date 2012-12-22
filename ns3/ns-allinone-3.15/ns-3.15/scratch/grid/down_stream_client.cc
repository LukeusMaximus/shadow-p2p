#include "down_stream_client.h"

namespace ns3 {

DownStreamClient::DownStreamClient() {

}

DownStreamClient::DownStreamClient(Ptr<Socket> sock, uint16_t port) {
    socket = sock;
    this->port = port;    
}

DownStreamClient::~DownStreamClient() {

}

void DownStreamClient::send(Ptr<Packet> packet) {
    std::list<Address>::iterator iter;
    for(iter = shout.begin(); iter != shout.end(); iter++) {
        Address address = *iter;
        socket->SendTo (packet, 0, InetSocketAddress (Ipv4Address::ConvertFrom(address), port));
        /*
        if (Ipv4Address::IsMatchingType (address)) {
            NS_LOG_INFO ("At time " << Simulator::Now ().GetSeconds () << "s client sent " << m_size << " bytes to " <<
                Ipv4Address::ConvertFrom (address) << " port " << port);
        } else if (Ipv6Address::IsMatchingType (address)) {
            NS_LOG_INFO ("At time " << Simulator::Now ().GetSeconds () << "s client sent " << m_size << " bytes to " <<
                Ipv6Address::ConvertFrom (address) << " port " << port);
        }
        */
    }
}

void DownStreamClient::setLocation(uint32_t x, uint32_t y) {
    location.x = x;
    location.y = y;
}

void DownStreamClient::setLocation(VirtualLocation loc) {
    location = loc;
}

VirtualLocation DownStreamClient::getLocation() {
    return location;
}

void DownStreamClient::addAddress(Address address) {
    shout.push_back(address);
}

}
