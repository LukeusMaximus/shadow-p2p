#include "ns3/core-module.h"
#include "ns3/network-module.h"
#include "ns3/internet-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/point-to-point-grid.h"
#include "ns3/applications-module.h"
#include "p2ps-helper.h"
#include "p2ps.h"

#define POINT() std::cout<<__FILE__<<":"<<__LINE__<<std::endl
#define DEBUG(s) std::cout<<__FILE__<<":"<<__LINE__<<":"<<s<<std::endl

using namespace ns3;

NS_LOG_COMPONENT_DEFINE ("GridScript");

int main (int argc, char *argv[]) {
    CommandLine cmd;
    cmd.Parse(argc, argv);

    LogComponentEnable("UdpEchoClientApplication", LOG_LEVEL_INFO);
    LogComponentEnable("UdpEchoServerApplication", LOG_LEVEL_INFO);

    PointToPointHelper pointToPoint;
    pointToPoint.SetDeviceAttribute("DataRate", StringValue("5Mbps"));
    pointToPoint.SetChannelAttribute("Delay", StringValue("2ms"));

    InternetStackHelper stack;
    Ipv4AddressHelper addressRow;
    addressRow.SetBase("10.1.0.0", "255.255.255.0");
    Ipv4AddressHelper addressCol;
    addressCol.SetBase("10.2.0.0", "255.255.255.0");

    PointToPointGridHelper gridHelper(10, 10, pointToPoint);
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
    dsc2.addAddress(gridHelper.GetIpv4Address(9,9));

    ShadowHelper shadowClient(gridHelper.GetIpv4Address(0,0), peerPort);
    shadowClient.SetAttribute("MaxPackets", UintegerValue(1));
    shadowClient.SetAttribute("Interval", TimeValue(Seconds(1.0)));
    shadowClient.SetAttribute("PacketSize", UintegerValue(1024));
    shadowClient.SetAttribute("SelfPort", UintegerValue(peerPort));
    
    
    ApplicationContainer clientApps = shadowClient.Install(gridHelper.GetNode(9,9));
    Ptr<Application> app = clientApps.Get(0);
    std::cout << "app " << app << std::endl;
    Application* appPtr = PeekPointer(app);
    std::cout << "appPtr " << appPtr << std::endl;
    ShadowClient* sc = (ShadowClient*)appPtr;
    std::cout << "sc " << sc << std::endl;
    sc->setEastClient(dsc1);
    
    clientApps.Start(Seconds(2.0));
    clientApps.Stop(Seconds(10.0));
    
    
    ShadowHelper shadowClient2(gridHelper.GetIpv4Address(9,9), peerPort);
    shadowClient2.SetAttribute("MaxPackets", UintegerValue(1));
    shadowClient2.SetAttribute("Interval", TimeValue(Seconds(1.0)));
    shadowClient2.SetAttribute("PacketSize", UintegerValue(1024));
    shadowClient2.SetAttribute("SelfPort", UintegerValue(peerPort));

    ApplicationContainer clientApps2 = shadowClient2.Install(gridHelper.GetNode(0,0));
    clientApps2.Start(Seconds(2.0));
    clientApps2.Stop(Seconds(10.0));

    Ipv4GlobalRoutingHelper::PopulateRoutingTables();
    
    pointToPoint.EnablePcapAll("grid");
    AsciiTraceHelper ascii;
    pointToPoint.EnableAsciiAll(ascii.CreateFileStream("grid_trace.tr"));

    Simulator::Run ();
    Simulator::Destroy ();
    return 0;
}
