#include "ns3/log.h"
#include "ns3/ipv4-address.h"
#include "ns3/ipv6-address.h"
#include "ns3/nstime.h"
#include "ns3/inet-socket-address.h"
#include "ns3/inet6-socket-address.h"
#include "ns3/socket.h"
#include "ns3/simulator.h"
#include "ns3/socket-factory.h"
#include "ns3/packet.h"
#include "ns3/uinteger.h"
#include "ns3/trace-source-accessor.h"
#include "shadowp2p.h"

namespace ns3 {

NS_LOG_COMPONENT_DEFINE ("UdpEchoClientApplication");
NS_OBJECT_ENSURE_REGISTERED (UdpEchoClient);

TypeId ShadowP2PClient::GetTypeId (void) {
    static TypeId tid = TypeId ("ShadowP2P")
        .SetParent<Application> ()
        .AddConstructor<ShadowP2PClient> ()
        .AddTraceSource ("Send", "A new packet is sent",
            MakeTraceSourceAccessor (&ShadowP2PClient::m_txTrace))
    ;
    return tid;
}

ShadowP2PClient::ShadowP2PClient () {
    NS_LOG_FUNCTION_NOARGS ();
    
    m_socket = 0;
}

ShadowP2PClient::~ShadowP2PClient() {
    NS_LOG_FUNCTION_NOARGS ();
}

void ShadowP2PClient::DoDispose (void) {
    NS_LOG_FUNCTION_NOARGS ();
    Application::DoDispose ();
}

void ShadowP2PClient::StartApplication (void) {
    NS_LOG_FUNCTION_NOARGS ();
    if (m_socket == 0)
    {
        TypeId tid = TypeId::LookupByName ("ns3::UdpSocketFactory");
        m_socket = Socket::CreateSocket (GetNode (), tid);
    }
    m_socket->SetRecvCallback (MakeCallback (&ShadowP2PClient::HandleRead, this));
    m_sendEvent = Simulator::Schedule (Seconds(0.), &ShadowP2PClient::TestSend, this);
}

void ShadowP2PClient::StopApplication () {
    NS_LOG_FUNCTION_NOARGS ();
}

void ShadowP2PClient::HandleRead (Ptr<Socket> socket) {
    NS_LOG_FUNCTION (this << socket);
}

void ShadowP2PClient::TestSend () {
    m_socket->SendTo()
}

} // Namespace ns3
