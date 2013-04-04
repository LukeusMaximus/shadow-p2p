from random import choice, randint, shuffle
import matplotlib.pyplot as plt

class ShoutGroup:
    def __init__(self, L, V, threshold):
        self.L = L
        self.V = V
        self.threshold = threshold
        self.reset_count = 0
        self.hard_reset()
    def reset(self):
        self.reset_count += 1
        self.v_remove_down = len(V)
        self.v_remove_up = 0
    def hard_reset(self):
        self.reset()
        self.reset_count = 0
    def give_list(self, S):
        i = 0;
        start = -1
        end = -1
        count = 0
        while start == -1 or end == -1:
            if S[i] in V:
                count += 1
                if count == self.threshold:
                    start = i+1
                elif count == len(self.V) - self.threshold:
                    end = i
            i += 1
        self.cut_off_iterations = randint(start, end)
    def respond(self, S):
        sv = [v for v in self.V if v in S]
        if len(sv) < self.v_remove_down:
            self.v_remove_up += 1
            self.v_remove_down -= 1
    
        if self.cut_off_iterations == 0:
            return 0
        else:
            self.cut_off_iterations -= 1
            return 1
        
        

class Adversary:
    def __init__(self, L, sg):
        self.L = L
        self.set_of_sets = []
        self.shout_group = sg
        self.number_of_v_removed = {}
        self.cumulative_paths = [0] * 4
    def attack(self):
        shuffle(L)
        S = list(L)
        self.shout_group.give_list(S)
        while self.shout_group.respond(S) == 1:
            S = S[1:]
        self.set_of_sets.append(S)
        a = self.shout_group.v_remove_up
        if a in self.number_of_v_removed:
            self.number_of_v_removed[a] += 1
        else:
            self.number_of_v_removed[a] = 1
        self.shout_group.reset()
    def tally(self):
        self.counts = [0] * len(L)
        for s in self.set_of_sets:
            for address in s:
                self.counts[address] += 1


if __name__ == "__main__":
    L = range(100)
    V = []
    while len(V) < 10:
        c = choice(L)
        if not (c in V):
            V.append(c)
    V.sort()
        
    sg = ShoutGroup(L, V, 3)
    ad = Adversary(L, sg)
    iterations = 1000
    for i in xrange(iterations):
        if i % (iterations / 100) == 0:
            print i
        ad.attack()
    ad.tally()
    
    print ad.cumulative_paths
    
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
    plt.xlabel("Address occurring in S")
    
    plt.figure(2)    
    cut_off_pos = [0] * (len(V)+1)
    for a in ad.number_of_v_removed:
        cut_off_pos[a] = ad.number_of_v_removed[a]
    plt.bar(range(len(V) + 1), cut_off_pos, width=1, color='g')
    plt.xlim(0,len(V))
    plt.xlabel("Number of addresses removed from V during attack")
    plt.ylabel("Times this number of addresses was removed")
    
    plt.show()
    raw_input()
    
    
