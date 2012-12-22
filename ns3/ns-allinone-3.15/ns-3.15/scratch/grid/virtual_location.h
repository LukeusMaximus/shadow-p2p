#ifndef VIRTUAL_LOCATION_H
#define VIRTUAL_LOCATION_H

#include <stdint.h>

namespace ns3 {

/**
 * Holds the information for a virtual location.
 * Basically x and y coordinates.
 */
class VirtualLocation {
public:
    VirtualLocation();
    VirtualLocation(uint32_t px, uint32_t py);
    virtual ~VirtualLocation();
    uint32_t x, y;
};

}

#endif // VIRTUAL_LOCATION_H
