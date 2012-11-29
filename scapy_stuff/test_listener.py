import os, socket, select, subprocess

def get_ip():
    output_proc = subprocess.check_output(["ip", "addr"])
    output_proc = output_proc.split("\n")
    ips = []
    for x in output_proc:
        if x.find("    inet ") == 0:
            ips.append(x[9:x.find("/")])
    for x in ips:
        if x != "127.0.0.1":
            return x
    return None

def run_listener(ip):
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    port = 1337
    s.bind((ip, port)) ## You also specify a specific intf, like '255.255.255.255' for Bcast or '127.0.0.1' for localhost...
    s.setblocking(0)
    
    while True:
        result = select.select([s],[],[])
        msg = result[0][0].recv(1024)
        print msg

if __name__ == "__main__":
    ip = get_ip()
    if ip != None:
        run_listener(ip)
    
