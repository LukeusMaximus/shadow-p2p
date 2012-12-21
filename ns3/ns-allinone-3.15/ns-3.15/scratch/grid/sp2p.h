#ifndef SHADOW_CLIENT_H
#define SHADOW_CLIENT_H

#include "ns3/application.h"
#include "ns3/event-id.h"
#include "ns3/ptr.h"
#include "ns3/ipv4-address.h"
#include "ns3/traced-callback.h"

namespace ns3 {

class Socket;
class Packet;

/**
 * Class holding a list of IP addresses
 */
class Shout {
public:
    Shout();
    virtual ~Shout();

    std::list<Address>::iterator getIterator();
    void addAddress(Address address);
private:
    std::list<Address> addresses;
};

/**
 * Shadow P2P client
 */
class ShadowClient : public Application {
public:
    static TypeId GetTypeId (void);

    ShadowClient ();

    virtual ~ShadowClient ();

    /**
    * \param ip destination ipv4 address
    * \param port destination port
    */
    void SetRemote (Address ip, uint16_t port);
    void SetRemote (Ipv4Address ip, uint16_t port);
    void SetRemote (Ipv6Address ip, uint16_t port);

    void SetOwnPort(uint16_t port);

    /**
    * Set the data size of the packet (the number of bytes that are sent as data
    * to the server).  The contents of the data are set to unspecified (don't
    * care) by this call.
    *
    * \warning If you have set the fill data for the echo client using one of the
    * SetFill calls, this will undo those effects.
    *
    * \param dataSize The size of the echo data you want to sent.
    */
    void SetDataSize (uint32_t dataSize);

    /**
    * Get the number of data bytes that will be sent to the server.
    *
    * \warning The number of bytes may be modified by calling any one of the 
    * SetFill methods.  If you have called SetFill, then the number of 
    * data bytes will correspond to the size of an initialized data buffer.
    * If you have not called a SetFill method, the number of data bytes will
    * correspond to the number of don't care bytes that will be sent.
    *
    * \returns The number of data bytes.
    */
    uint32_t GetDataSize (void) const;

protected:
    virtual void DoDispose (void);

private:

    virtual void StartApplication (void);
    virtual void StopApplication (void);

    void ScheduleTransmit (Time dt);
    void Send (void);

    void HandleRead (Ptr<Socket> socket);

    uint16_t m_ownPort;

    uint32_t m_count;
    Time m_interval;
    uint32_t m_size;

    uint32_t m_dataSize;
    uint8_t *m_data;

    uint32_t m_sent;
    Ptr<Socket> m_socket;
    Address m_peerAddress;
    uint16_t m_peerPort;
    EventId m_sendEvent;
    /// Callbacks for tracing the packet Tx events
    TracedCallback<Ptr<const Packet> > m_txTrace;
};

} // namespace ns3

#endif /* SHADOW_CLIENT_H */
