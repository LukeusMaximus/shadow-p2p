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

#ifndef SHADOW_CLIENT_H
#define SHADOW_CLIENT_H

#include "ns3/application.h"
#include "ns3/event-id.h"
#include "ns3/ptr.h"
#include "ns3/ipv4-address.h"
#include "ns3/traced-callback.h"
#include "down_stream_client.h"

namespace ns3 {

class Socket;
class Packet;

/**
 * A Shadow P2P client
 */
class ShadowClient : public Application {
public:
    static TypeId GetTypeId (void);
    ShadowClient ();
    virtual ~ShadowClient ();
    void SetRemote (Address ip, uint16_t port);
    void SetRemote (Ipv4Address ip, uint16_t port);
    void SetRemote (Ipv6Address ip, uint16_t port);
    void SetDataSize (uint32_t dataSize);
    uint32_t GetDataSize (void) const;
    
    void setEastClient(DownStreamClient client);
    void setNorthClient(DownStreamClient client);

protected:
    virtual void DoDispose (void);

private:

    virtual void StartApplication (void);
    virtual void StopApplication (void);

    void ScheduleTransmit (Time dt);
    void Send (void);

    void HandleRead (Ptr<Socket> socket);

    uint16_t m_selfPort;
    std::list<Address> selfShout;
    DownStreamClient eastClient;
    DownStreamClient northClient;

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
