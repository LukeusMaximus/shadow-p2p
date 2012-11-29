from time import sleep
from test_listener import get_ip
import os, socket, sys, struct
from scapy.all import send, IP, UDP
from scapy.config import conf
from scapy.supersocket import L3RawSocket
from scapy.error import log_runtime

def run_sender(ip, interval):
    i = 0
    while True:
        msg = "Luke @ " + str(socket.gethostname()) + " " + str(i)
        port = 1337
        dest_ip = ip + "/26"
        print dest_ip
        packet = IP(src = "1.2.3.4", dst = dest_ip)/UDP(sport = 0, dport = port)/msg
        send(packet)
        sleep(interval)
        i += 1

if __name__ == "__main__":
    conf.L3socket = L3RawSocket
    ip = get_ip()
    if ip != None and len(sys.argv) > 1:
        run_sender(ip, int(sys.argv[1]))
