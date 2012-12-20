#ifndef SHADOW_P2P_CLIENT_H
#define SHADOW_P2P_CLIENT_H

#include "ns3/application.h"
#include "ns3/event-id.h"
#include "ns3/ptr.h"
#include "ns3/ipv4-address.h"
#include "ns3/traced-callback.h"

namespace ns3 {

class Socket;
class Packet;

/**
 * A shout is simply a list of IP addresses.
 */
class Shout {
public:
    int getNAddresses();
    
private:
    std::list<Address> addresses;
}

/**
 * This is a prototype shadow p2p client
 */
class ShadowP2PClient : public Application 
{
public:
    static TypeId GetTypeId (void);
    ShadowP2PClient ();
    void TestSend();

    virtual ~ShadowP2PClientt ();

protected:
    virtual void DoDispose (void);

private:

    virtual void StartApplication (void);
    virtual void StopApplication (void);

    void HandleRead (Ptr<Socket> socket);
  
    // -- Members --
  
    // Callbacks for tracing the packet Tx events
    TracedCallback<Ptr<const Packet> > m_txTrace;
    
    Ptr<Socket> m_socket;
    EventId m_sendEvent;
};

} // namespace ns3

#endif /* SHADOW_P2P_CLIENT_H */
