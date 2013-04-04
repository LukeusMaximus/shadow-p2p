from random import choice, shuffle
import matplotlib.pyplot as plt

class PseudoAttackResultGenerator:
    def __init__(self, L, V):
        self.L = L
        self.V = V
    def generate(self):
        shuffle(L)
        S = set(L[:len(L)/2])
        xs = 0
        for v in V:
            if v in S:
                xs += 1
        return xs


if __name__ == "__main__":
    L = range(10000)
    V = []
    while len(V) < 100:
        c = choice(L)
        if not (c in V):
            V.append(c)
        
    gen = PseudoAttackResultGenerator(L,V)    
    results = [0] * (len(V) + 1)
    iterations = 100000
    for i in xrange(iterations):
        if i % (iterations / 100) == 0:
            print i
        r = gen.generate()
        results[r] += 1
    print results
    V.sort()
    print V
    fig = plt.figure()
    ax = fig.add_subplot(111)
    ax.plot(range(0,len(V)+1), results)
    fig.show()
    raw_input()
    
    
