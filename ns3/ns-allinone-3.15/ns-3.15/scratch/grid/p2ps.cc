/* -*- Mode:C++; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */
/*
 * Copyright 2007 University of Washington
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
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
#include "p2ps.h"

namespace ns3 {

NS_LOG_COMPONENT_DEFINE ("ShadowClientApplication");
NS_OBJECT_ENSURE_REGISTERED (ShadowClient);

TypeId ShadowClient::GetTypeId (void) {
    static TypeId tid = TypeId ("ns3::ShadowClient")
        .SetParent<Application> ()
        .AddConstructor<ShadowClient> ()
        .AddAttribute ("MaxPackets", 
            "The maximum number of packets the application will send",
            UintegerValue (100),
            MakeUintegerAccessor (&ShadowClient::m_count),
            MakeUintegerChecker<uint32_t> ())
        .AddAttribute ("Interval", 
            "The time to wait between packets",
            TimeValue (Seconds (1.0)),
            MakeTimeAccessor (&ShadowClient::m_interval),
            MakeTimeChecker ())
        .AddAttribute ("SelfPort", 
            "The destination port of the outbound packets",
            UintegerValue (0),
            MakeUintegerAccessor (&ShadowClient::m_selfPort),
            MakeUintegerChecker<uint16_t> ())
        .AddAttribute ("RemoteAddress", 
            "The destination Address of the outbound packets",
            AddressValue (),
            MakeAddressAccessor (&ShadowClient::m_peerAddress),
            MakeAddressChecker ())
        .AddAttribute ("RemotePort", 
            "The destination port of the outbound packets",
            UintegerValue (0),
            MakeUintegerAccessor (&ShadowClient::m_peerPort),
            MakeUintegerChecker<uint16_t> ())
        .AddAttribute ("PacketSize", "Size of echo data in outbound packets",
            UintegerValue (100),
            MakeUintegerAccessor (&ShadowClient::SetDataSize, &ShadowClient::GetDataSize),
            MakeUintegerChecker<uint32_t> ())
        .AddTraceSource ("Tx", "A new packet is created and is sent",
            MakeTraceSourceAccessor (&ShadowClient::m_txTrace))
    ;
    return tid;
}

ShadowClient::ShadowClient () {
    NS_LOG_FUNCTION_NOARGS ();
    m_sent = 0;
    m_socket = 0;
    m_sendEvent = EventId ();
    m_data = 0;
    m_dataSize = 0;
}

ShadowClient::~ShadowClient() {
    NS_LOG_FUNCTION_NOARGS ();
    m_socket = 0;

    delete [] m_data;
    m_data = 0;
    m_dataSize = 0;
}

void ShadowClient::SetRemote (Address ip, uint16_t port) {
    m_peerAddress = ip;
    m_peerPort = port;
}

void ShadowClient::SetRemote (Ipv4Address ip, uint16_t port) {
    m_peerAddress = Address (ip);
    m_peerPort = port;
}

void ShadowClient::SetRemote (Ipv6Address ip, uint16_t port) {
    m_peerAddress = Address (ip);
    m_peerPort = port;
}

void ShadowClient::setEastClient(DownStreamClient client) {
    eastClient = client;
}

void ShadowClient::setNorthClient(DownStreamClient client) {
    northClient = client;
}

void ShadowClient::DoDispose (void) {
    NS_LOG_FUNCTION_NOARGS ();
    Application::DoDispose ();
}

void ShadowClient::StartApplication (void) {
    NS_LOG_FUNCTION_NOARGS ();

    if (m_socket == 0) {
        TypeId tid = TypeId::LookupByName ("ns3::UdpSocketFactory");
        m_socket = Socket::CreateSocket (GetNode (), tid);
        if (Ipv4Address::IsMatchingType(m_peerAddress) == true) {
            m_socket->Bind(InetSocketAddress (m_selfPort));
        } else if (Ipv6Address::IsMatchingType(m_peerAddress) == true) {
            m_socket->Bind(Inet6SocketAddress (m_selfPort));
        }
    }

    m_socket->SetRecvCallback (MakeCallback (&ShadowClient::HandleRead, this));

    ScheduleTransmit (Seconds (0.));
}

void ShadowClient::StopApplication () {
    NS_LOG_FUNCTION_NOARGS ();

    if (m_socket != 0) {
        m_socket->Close ();
        m_socket->SetRecvCallback (MakeNullCallback<void, Ptr<Socket> > ());
        m_socket = 0;
    }

    Simulator::Cancel (m_sendEvent);
}

void ShadowClient::SetDataSize (uint32_t dataSize) {
    NS_LOG_FUNCTION (dataSize);

    //
    // If the client is setting the echo packet data size this way, we infer
    // that she doesn't care about the contents of the packet at all, so 
    // neither will we.
    //
    delete [] m_data;
    m_data = 0;
    m_dataSize = 0;
    m_size = dataSize;
}

uint32_t ShadowClient::GetDataSize (void) const {
    NS_LOG_FUNCTION_NOARGS ();
    return m_size;
}

void ShadowClient::ScheduleTransmit (Time dt) {
    NS_LOG_FUNCTION_NOARGS ();
    m_sendEvent = Simulator::Schedule (dt, &ShadowClient::Send, this);
}

void ShadowClient::Send (void) {
    NS_LOG_FUNCTION_NOARGS ();

    NS_ASSERT (m_sendEvent.IsExpired ());

    Ptr<Packet> p;
    if (m_dataSize) {
        //
        // If m_dataSize is non-zero, we have a data buffer of the same size that we
        // are expected to copy and send.  This state of affairs is created if one of
        // the Fill functions is called.  In this case, m_size must have been set
        // to agree with m_dataSize
        //
        NS_ASSERT_MSG (m_dataSize == m_size, "ShadowClient::Send(): m_size and m_dataSize inconsistent");
        NS_ASSERT_MSG (m_data, "ShadowClient::Send(): m_dataSize but no m_data");
        p = Create<Packet> (m_data, m_dataSize);
    } else {
        //
        // If m_dataSize is zero, the client has indicated that she doesn't care 
        // about the data itself either by specifying the data size by setting
        // the corresponding atribute or by not calling a SetFill function.  In 
        // this case, we don't worry about it either.  But we do allow m_size
        // to have a value different from the (zero) m_dataSize.
        //
        p = Create<Packet> (m_size);
    }
    // call to the trace sinks before the packet is actually sent,
    // so that tags added to the packet can be sent as well
    m_txTrace (p);
    //m_socket->SendTo (p, 0, InetSocketAddress (Ipv4Address::ConvertFrom(m_peerAddress), m_peerPort));
    eastClient.send(m_socket, p);
    
    ++m_sent;

    //NS_LOG_INFO("East client shout size: " << eastClient.nAddresses());
    
    
    if (Ipv4Address::IsMatchingType (m_peerAddress)) {
        NS_LOG_INFO ("At time " << Simulator::Now ().GetSeconds () << "s client sent " << m_size << " bytes to " <<
            Ipv4Address::ConvertFrom (m_peerAddress) << " port " << m_peerPort);
    } else if (Ipv6Address::IsMatchingType (m_peerAddress)) {
        NS_LOG_INFO ("At time " << Simulator::Now ().GetSeconds () << "s client sent " << m_size << " bytes to " <<
            Ipv6Address::ConvertFrom (m_peerAddress) << " port " << m_peerPort);
    }

    if (m_sent < m_count) {
        ScheduleTransmit (m_interval);
    }
}

void ShadowClient::HandleRead (Ptr<Socket> socket) {
    NS_LOG_FUNCTION (this << socket);
    Ptr<Packet> packet;
    Address from;
    while ((packet = socket->RecvFrom (from))) {
        if (InetSocketAddress::IsMatchingType (from)) {
            NS_LOG_INFO ("At time " << Simulator::Now ().GetSeconds () << "s client received " << packet->GetSize () << " bytes from " <<
                InetSocketAddress::ConvertFrom (from).GetIpv4 () << " port " <<
                InetSocketAddress::ConvertFrom (from).GetPort ());
        } else if (Inet6SocketAddress::IsMatchingType (from)) {
            NS_LOG_INFO ("At time " << Simulator::Now ().GetSeconds () << "s client received " << packet->GetSize () << " bytes from " <<
                Inet6SocketAddress::ConvertFrom (from).GetIpv6 () << " port " <<
                Inet6SocketAddress::ConvertFrom (from).GetPort ());
        }
    }
}

} // Namespace ns3
