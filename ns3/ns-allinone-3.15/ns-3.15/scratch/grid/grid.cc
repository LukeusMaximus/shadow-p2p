#include "ns3/core-module.h"
#include "ns3/network-module.h"
#include "ns3/internet-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/point-to-point-grid.h"
#include "ns3/applications-module.h"
#include "p2ps-helper.h"
#include "p2ps.h"

#define POINT() std::cout<<__FILE__<<":"<<__LINE__<<std::endl
#define DEBUG(s) std::cout<<"["<<__FILE__<<":"<<__LINE__<<"] "<<s<<std::endl

using namespace ns3;

NS_LOG_COMPONENT_DEFINE ("GridScript");

int main (int argc, char *argv[]) {
    CommandLine cmd;
    cmd.Parse(argc, argv);

    LogComponentEnable("ShadowClientApplication", LOG_LEVEL_INFO);

    PointToPointHelper pointToPoint;
    pointToPoint.SetDeviceAttribute("DataRate", StringValue("5Mbps"));
    pointToPoint.SetChannelAttribute("Delay", StringValue("2ms"));
    
    InternetStackHelper stack;
    Ipv4AddressHelper addressRow;
    addressRow.SetBase("10.0.0.0", "255.255.255.252", "0.0.0.1");
    Ipv4AddressHelper addressCol;
    addressCol.SetBase("10.128.0.0", "255.255.255.252", "0.0.0.1");

    PointToPointGridHelper gridHelper(20, 20, pointToPoint);
    gridHelper.InstallStack(stack);
    gridHelper.AssignIpv4Addresses(addressRow, addressCol);
    
    /*
    UdpEchoServerHelper echoServer(9);

    ApplicationContainer serverApps = echoServer.Install (gridHelper.GetNode(0,0));
    serverApps.Start(Seconds(1.0));
    serverApps.Stop(Seconds(10.0));
    */

    uint16_t peerPort = 9;

    DownStreamClient dsc1(peerPort);
    dsc1.addAddress(gridHelper.GetIpv4Address(0,0));
    DownStreamClient dsc2(peerPort);
    dsc2.addAddress(gridHelper.GetIpv4Address(19,19));

    ShadowHelper shadowClient(gridHelper.GetIpv4Address(0,0), peerPort);
    shadowClient.SetAttribute("MaxPackets", UintegerValue(1));
    shadowClient.SetAttribute("Interval", TimeValue(Seconds(1.0)));
    shadowClient.SetAttribute("PacketSize", UintegerValue(1024));
    shadowClient.SetAttribute("SelfPort", UintegerValue(peerPort));
    
    
    ApplicationContainer clientApps = shadowClient.Install(gridHelper.GetNode(19,19));
    ShadowClient* sc = (ShadowClient*)PeekPointer(clientApps.Get(0));
    sc->setEastClient(dsc1);
    clientApps.Start(Seconds(2.0));
    clientApps.Stop(Seconds(10.0));
    
    
    ShadowHelper shadowClient2(gridHelper.GetIpv4Address(19,19), peerPort);
    shadowClient2.SetAttribute("MaxPackets", UintegerValue(1));
    shadowClient2.SetAttribute("Interval", TimeValue(Seconds(1.0)));
    shadowClient2.SetAttribute("PacketSize", UintegerValue(1024));
    shadowClient2.SetAttribute("SelfPort", UintegerValue(peerPort));

    ApplicationContainer clientApps2 = shadowClient2.Install(gridHelper.GetNode(0,0));
    sc = (ShadowClient*)PeekPointer(clientApps2.Get(0));
    sc->setEastClient(dsc2);
    clientApps2.Start(Seconds(2.0));
    clientApps2.Stop(Seconds(10.0));

    Ipv4GlobalRoutingHelper::PopulateRoutingTables();
    
    //pointToPoint.EnablePcapAll("grid");
    AsciiTraceHelper ascii;
    pointToPoint.EnableAsciiAll(ascii.CreateFileStream("grid_trace.tr"));

    Simulator::Run ();
    Simulator::Destroy ();
    return 0;
}
