from time import sleep
from test_listener import get_ip
import os 
import socket
import sys

def run_sender(ip, interval):
    outsock =  socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    outsock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    bcast = "255.255.255.255"
    i = 0
    
    while True:
        msg = "Luke @ " + str(socket.gethostname()) + " " + str(i)
        port = 1337
        outsock.sendto(msg, (ip, port))
        sleep(interval)
        i += 1

if __name__ == "__main__":
    ip = get_ip()
    if ip != None and len(sys.argv) > 1:
        run_sender(ip, int(sys.argv[1]))
