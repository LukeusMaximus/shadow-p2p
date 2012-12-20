#include "ns3/core-module.h"
#include "ns3/network-module.h"
#include "ns3/internet-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/point-to-point-grid.h"
#include "ns3/applications-module.h"

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

    UdpEchoServerHelper echoServer(9);

    ApplicationContainer serverApps = echoServer.Install (gridHelper.GetNode(0,0));
    serverApps.Start(Seconds(1.0));
    serverApps.Stop(Seconds(10.0));

    std::cout << gridHelper.GetIpv4Address(0,0) << std::endl;

    UdpEchoClientHelper echoClient (gridHelper.GetIpv4Address(0,0), 9);
    echoClient.SetAttribute ("MaxPackets", UintegerValue(1));
    echoClient.SetAttribute ("Interval", TimeValue(Seconds(1.0)));
    echoClient.SetAttribute ("PacketSize", UintegerValue(1024));

    ApplicationContainer clientApps = echoClient.Install(gridHelper.GetNode(9,9));
    clientApps.Start (Seconds (2.0));
    clientApps.Stop (Seconds (10.0));

    Ipv4GlobalRoutingHelper::PopulateRoutingTables();
    
    pointToPoint.EnablePcapAll("grid");
    AsciiTraceHelper ascii;
    pointToPoint.EnableAsciiAll(ascii.CreateFileStream("grid_trace.tr"));

    Simulator::Run ();
    Simulator::Destroy ();
    return 0;
}
