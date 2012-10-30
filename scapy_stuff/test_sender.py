from time import sleep
import os 
import socket

if __name__ == "__main__":
    outsock =  socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    outsock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    bcast = "255.255.255.255"
    i = 0
    
    while True:
        msg = "FOO_Master @ " + str(socket.gethostname()) + " " + str(i)
        port = 1337
        outsock.sendto(msg, ('192.168.1.137', port))
        sleep(10)
        i += 1
