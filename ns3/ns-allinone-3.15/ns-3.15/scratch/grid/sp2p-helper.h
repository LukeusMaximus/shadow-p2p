#ifndef SHADOW_HELPER_H
#define SHADOW_HELPER_H

#include <stdint.h>
#include "ns3/application-container.h"
#include "ns3/node-container.h"
#include "ns3/object-factory.h"
#include "ns3/ipv4-address.h"
#include "ns3/ipv6-address.h"

namespace ns3 {

/**
 * \brief create an application which sends a udp packet and waits for an echo of this packet
 */
class ShadowHelper
{
public:
    /**
    * Create UdpEchoClientHelper which will make life easier for people trying
    * to set up simulations with echos.
    *
    * \param ip The IP address of the remote udp echo server
    * \param port The port number of the remote udp echo server
    */
    ShadowHelper (Address ip, uint16_t port);
    ShadowHelper (Ipv4Address ip, uint16_t port);
    ShadowHelper (Ipv6Address ip, uint16_t port);

    /**
    * Record an attribute to be set in each Application after it is is created.
    *
    * \param name the name of the attribute to set
    * \param value the value of the attribute to set
    */
    void SetAttribute (std::string name, const AttributeValue &value);

    /**
    * Given a pointer to a UdpEchoClient application, set the data fill of the 
    * packet (what is sent as data to the server) to the contents of the fill
    * string (including the trailing zero terminator).
    *
    * \warning The size of resulting echo packets will be automatically adjusted
    * to reflect the size of the fill string -- this means that the PacketSize
    * attribute may be changed as a result of this call.
    *
    * \param app Smart pointer to the application (real type must be UdpEchoClient).
    * \param fill The string to use as the actual echo data bytes.
    */
    void SetFill (Ptr<Application> app, std::string fill);

    /**
    * Given a pointer to a UdpEchoClient application, set the data fill of the 
    * packet (what is sent as data to the server) to the contents of the fill
    * byte.
    *
    * The fill byte will be used to initialize the contents of the data packet.
    * 
    * \warning The size of resulting echo packets will be automatically adjusted
    * to reflect the dataLength parameter -- this means that the PacketSize
    * attribute may be changed as a result of this call.
    *
    * \param app Smart pointer to the application (real type must be UdpEchoClient).
    * \param fill The byte to be repeated in constructing the packet data..
    * \param dataLength The desired length of the resulting echo packet data.
    */
    void SetFill (Ptr<Application> app, uint8_t fill, uint32_t dataLength);

    /**
    * Given a pointer to a UdpEchoClient application, set the data fill of the 
    * packet (what is sent as data to the server) to the contents of the fill
    * buffer, repeated as many times as is required.
    *
    * Initializing the fill to the contents of a single buffer is accomplished
    * by providing a complete buffer with fillLength set to your desired 
    * dataLength
    *
    * \warning The size of resulting echo packets will be automatically adjusted
    * to reflect the dataLength parameter -- this means that the PacketSize
    * attribute of the Application may be changed as a result of this call.
    *
    * \param app Smart pointer to the application (real type must be UdpEchoClient).
    * \param fill The fill pattern to use when constructing packets.
    * \param fillLength The number of bytes in the provided fill pattern.
    * \param dataLength The desired length of the final echo data.
    */
    void SetFill (Ptr<Application> app, uint8_t *fill, uint32_t fillLength, uint32_t dataLength);

    /**
    * Create a udp echo client application on the specified node.  The Node
    * is provided as a Ptr<Node>.
    *
    * \param node The Ptr<Node> on which to create the UdpEchoClientApplication.
    *
    * \returns An ApplicationContainer that holds a Ptr<Application> to the 
    *          application created
    */
    ApplicationContainer Install (Ptr<Node> node) const;

    /**
    * Create a udp echo client application on the specified node.  The Node
    * is provided as a string name of a Node that has been previously 
    * associated using the Object Name Service.
    *
    * \param nodeName The name of the node on which to create the UdpEchoClientApplication
    *
    * \returns An ApplicationContainer that holds a Ptr<Application> to the 
    *          application created
    */
    ApplicationContainer Install (std::string nodeName) const;

    /**
    * \param c the nodes
    *
    * Create one udp echo client application on each of the input nodes
    *
    * \returns the applications created, one application per input node.
    */
    ApplicationContainer Install (NodeContainer c) const;

private:
    Ptr<Application> InstallPriv (Ptr<Node> node) const;
    ObjectFactory m_factory;
};

} // namespace ns3

#endif /* SHADOW_HELPER_H */
