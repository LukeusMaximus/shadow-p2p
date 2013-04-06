from random import choice, randint, shuffle
import matplotlib.pyplot as plt

class ShoutGroup:
    def __init__(self, L, V):
        self.L = L
        self.V = V
        self.reset_count = 0
        self.hard_reset()
    def reset(self):
        self.reset_count += 1
        self.previous_count = len(self.V)
        self.random_cut_off = -1
        self.calls_between = [0] * ((len(self.V)/2)-1)
        self.calls_between_index = -1
    def hard_reset(self):
        self.reset()
        self.reset_count = 0
    def respond(self, S):
        sv = [v for v in self.V if v in S]
        if len(sv) >= self.previous_count:
            if self.random_cut_off == -1:
                self.calls_between[self.calls_between_index] += 1
                return 1
            else:
                if self.random_cut_off > 0:
                    self.random_cut_off -= 1
                    return 1
                else:
                    return 0
        elif len(sv) == self.previous_count -1:
            if self.random_cut_off != -1:
                return 0
            self.previous_count = len(sv)
            self.calls_between_index += 1
            if self.calls_between_index == len(self.calls_between):
                self.random_cut_off = randint(0, self.estimate_search())
            return 1
        else:
            return 0
    def estimate_search(self):
        return sum(self.calls_between) / len(self.calls_between)
        
        

class Adversary:
    def __init__(self, L, sg):
        self.L = L
        self.set_of_sets = []
        self.shout_group = sg
    def attack(self):
        shuffle(L)
        S = list(L)
        while self.shout_group.respond(S) == 1:
            S = S[1:]
        self.set_of_sets.append(S)
        self.shout_group.reset()
    def tally(self):
        self.counts = [0] * len(L)
        for s in self.set_of_sets:
            for address in s:
                self.counts[address] += 1


if __name__ == "__main__":
    L = range(100)
    V = []
    while len(V) < 20:
        c = choice(L)
        if not (c in V):
            V.append(c)
        
    sg = ShoutGroup(L, V)
    ad = Adversary(L, sg)
    for i in xrange(1000):
        if i % 10 == 0:
            print i
        ad.attack()
    ad.tally()
    
    sorted_tally = [(0,0)] * len(ad.counts)
    for i in xrange(len(ad.counts)):
        a = 0
        if i in V:
            a = 1
        sorted_tally[i] = (i, ad.counts[i], a)        
    sorted_tally = sorted(sorted_tally, key=lambda x: x[1])
    
    print V
    print sorted_tally
    
    plt.figure(1)
    plt.subplot(211)
    plt.bar(range(len(L)), [x[1] for x in sorted_tally], width = 1, lw=0)
    plt.ylabel("Number of appearances")
    plt.subplot(212)
    plt.scatter(range(len(L)), [x[2] for x in sorted_tally], lw=0)
    plt.xlim(0, len(L))
    plt.ylabel("Is address in shout group")
    plt.yticks([0,1])
    plt.xlabel("Address occurring in S")
    plt.show()
    raw_input()
    
    
