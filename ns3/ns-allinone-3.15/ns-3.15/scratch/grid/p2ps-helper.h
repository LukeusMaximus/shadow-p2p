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
class ShadowHelper {
public:
  ShadowHelper (Address ip, uint16_t port);
  ShadowHelper (Ipv4Address ip, uint16_t port);
  ShadowHelper (Ipv6Address ip, uint16_t port);
  void SetAttribute (std::string name, const AttributeValue &value);
  ApplicationContainer Install (Ptr<Node> node) const;
  ApplicationContainer Install (std::string nodeName) const;
  ApplicationContainer Install (NodeContainer c) const;

private:
  Ptr<Application> InstallPriv (Ptr<Node> node) const;
  ObjectFactory m_factory;
};

} // namespace ns3

#endif /* SHADOW_HELPER_H */
