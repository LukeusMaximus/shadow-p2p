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

void setup4NodeNetwork(PointToPointGridHelper* gridHelper, uint16_t peerPort, unsigned int* xs, unsigned int* ys) {
/*

    ^    ^
    |    |
    2--->3--->
    ^    ^
    |    |
    0--->1--->

*/
    DownStreamClient* dscs[4];
    unsigned int i;
    for(i = 0; i < 4; ++i) {
        DownStreamClient* dsc = new DownStreamClient(peerPort);
        dsc->addAddress(gridHelper->GetIpv4Address(xs[i],ys[i]));
        dscs[i] = dsc;
    }
    
    for(i = 0; i < 4; ++i) {
        ShadowHelper shadowClient(gridHelper->GetIpv4Address(0, 0), peerPort); // We aren't using the address set.
        shadowClient.SetAttribute("MaxPackets", UintegerValue(1));
        shadowClient.SetAttribute("Interval", TimeValue(Seconds(1.0)));
        shadowClient.SetAttribute("PacketSize", UintegerValue(1024));
        shadowClient.SetAttribute("SelfPort", UintegerValue(peerPort));
        
        ApplicationContainer clientApps = shadowClient.Install(gridHelper->GetNode(xs[i], ys[i]));
        ShadowClient* sc = (ShadowClient*)PeekPointer(clientApps.Get(0));
        unsigned int east = ((i/2) * 2) + ((i + 1) % 2);
        unsigned int north = (i + 2) % 4;
        sc->setEastClient(*dscs[east]);
        sc->setNorthClient(*dscs[north]);
        clientApps.Start(Seconds(2.0));
        clientApps.Stop(Seconds(10.0));
    }
}

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

    PointToPointGridHelper gridHelper(10, 10, pointToPoint);
    gridHelper.InstallStack(stack);
    gridHelper.AssignIpv4Addresses(addressRow, addressCol);
    
    uint16_t peerPort = 9;
    
    
    unsigned int xs[] = {4, 0, 9, 6};
    unsigned int ys[] = {7, 2, 2, 5};
    setup4NodeNetwork(&gridHelper, peerPort, xs, ys);
    
    /*
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
    ShadowClient* sc = (ShadowClient*)PeekPointer(clientApps.Get(0));
    sc->setEastClient(dsc1);
    clientApps.Start(Seconds(2.0));
    clientApps.Stop(Seconds(10.0));
    
    
    ShadowHelper shadowClient2(gridHelper.GetIpv4Address(9,9), peerPort);
    shadowClient2.SetAttribute("MaxPackets", UintegerValue(1));
    shadowClient2.SetAttribute("Interval", TimeValue(Seconds(1.0)));
    shadowClient2.SetAttribute("PacketSize", UintegerValue(1024));
    shadowClient2.SetAttribute("SelfPort", UintegerValue(peerPort));

    ApplicationContainer clientApps2 = shadowClient2.Install(gridHelper.GetNode(0,0));
    sc = (ShadowClient*)PeekPointer(clientApps2.Get(0));
    sc->setEastClient(dsc2);
    clientApps2.Start(Seconds(2.0));
    clientApps2.Stop(Seconds(10.0));
    */

    Ipv4GlobalRoutingHelper::PopulateRoutingTables();
    
    //pointToPoint.EnablePcapAll("grid");
    AsciiTraceHelper ascii;
    pointToPoint.EnableAsciiAll(ascii.CreateFileStream("grid_trace.tr"));

    Simulator::Run ();
    Simulator::Destroy ();
    return 0;
}
