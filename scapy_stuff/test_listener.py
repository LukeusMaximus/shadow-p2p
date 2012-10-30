import os, socket, select

if __name__ == "__main__":
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.bind(('192.168.1.137', 1337)) ## You also specify a specific intf, like '255.255.255.255' for Bcast or '127.0.0.1' for localhost...
    s.setblocking(0)

    while True:
        result = select.select([s],[],[])
        msg = result[0][0].recv(1024) 
        print msg
