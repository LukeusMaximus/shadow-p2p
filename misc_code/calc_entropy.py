from math import log

def calc_entropy(dist):
    parts = [0] * len(dist)
    for i in xrange(len(dist)):
        if dist[i] == 0:
            parts[i] = 0
        else:
            parts[i] = dist[i]*log(dist[i],2)
    return -sum(parts)

