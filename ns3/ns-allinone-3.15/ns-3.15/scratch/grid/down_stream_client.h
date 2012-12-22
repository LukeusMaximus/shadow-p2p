#ifndef DOWN_STREAM_CLIENT_H
#define DOWN_STREAM_CLIENT_H

#include "ns3/ipv4-address.h"
#include "ns3/inet-socket-address.h"
#include "ns3/socket.h"
#include "ns3/packet.h"
#include "virtual_location.h"

namespace ns3 {

/**
 * Holds all the information required to send data to a client downstream.
 * That being the virtual location and the shout information.
 */
class DownStreamClient {
public:
    DownStreamClient();
    DownStreamClient(Ptr<Socket> sock, uint16_t port);
    virtual ~DownStreamClient();
    void send(Ptr<Packet> packet);
    void setLocation(uint32_t x, uint32_t y);
    void setLocation(VirtualLocation loc);
    VirtualLocation getLocation();
    void addAddress(Address address);
private:
    std::list<Address> shout;
    VirtualLocation location;
    Ptr<Socket> socket;
    uint16_t port;
};

}

#endif //DOWN_STREAM_CLIENT_H
