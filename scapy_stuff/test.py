import sys
from scapy.all import sr1,IP,ICMP

if __name__ == "__main__":
    p=sr1(IP(dst="www.slashdot.org")/ICMP()/"XXXXXXXXXXX")
    if p:
        p.show()
