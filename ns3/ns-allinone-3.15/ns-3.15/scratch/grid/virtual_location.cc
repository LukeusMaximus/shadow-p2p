#include "virtual_location.h"

namespace ns3 {

VirtualLocation::VirtualLocation() {
    x = 0;
    y = 0;
}

VirtualLocation::VirtualLocation(uint32_t px, uint32_t py) {
    x = px;
    y = py;
}

VirtualLocation::~VirtualLocation() {

}

}
