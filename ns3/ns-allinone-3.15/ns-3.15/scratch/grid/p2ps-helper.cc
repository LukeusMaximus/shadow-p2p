#include "p2ps-helper.h"
#include "p2ps.h"
#include "ns3/uinteger.h"
#include "ns3/names.h"

namespace ns3 {
ShadowHelper::ShadowHelper (Address address, uint16_t port) {
    m_factory.SetTypeId (ShadowClient::GetTypeId ());
    SetAttribute ("RemoteAddress", AddressValue (address));
    SetAttribute ("RemotePort", UintegerValue (port));
}

ShadowHelper::ShadowHelper (Ipv4Address address, uint16_t port) {
    m_factory.SetTypeId (ShadowClient::GetTypeId ());
    SetAttribute ("RemoteAddress", AddressValue (Address(address)));
    SetAttribute ("RemotePort", UintegerValue (port));
}

ShadowHelper::ShadowHelper (Ipv6Address address, uint16_t port) {
    m_factory.SetTypeId (ShadowClient::GetTypeId ());
    SetAttribute ("RemoteAddress", AddressValue (Address(address)));
    SetAttribute ("RemotePort", UintegerValue (port));
}

void ShadowHelper::SetAttribute (std::string name, const AttributeValue &value) {
    m_factory.Set (name, value);
}

ApplicationContainer ShadowHelper::Install (Ptr<Node> node) const {
    return ApplicationContainer (InstallPriv (node));
}

ApplicationContainer ShadowHelper::Install (std::string nodeName) const {
    Ptr<Node> node = Names::Find<Node> (nodeName);
    return ApplicationContainer (InstallPriv (node));
}

ApplicationContainer ShadowHelper::Install (NodeContainer c) const {
    ApplicationContainer apps;
    for (NodeContainer::Iterator i = c.Begin (); i != c.End (); ++i) {
        apps.Add (InstallPriv (*i));
    }

    return apps;
}

Ptr<Application> ShadowHelper::InstallPriv (Ptr<Node> node) const {
    Ptr<Application> app = m_factory.Create<ShadowClient> ();
    node->AddApplication (app);

    return app;
}

} // namespace ns3
