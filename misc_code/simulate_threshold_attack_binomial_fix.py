from random import choice, randint, shuffle, gauss
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
        self.v_remove_count_down = len(self.V)
        self.v_remove_count_up = 0
        self.random_cut_off = -1
        self.calls_between = [0] * (self.threshold-1)
        self.calls_between_index = -1
        self.return_path_counts = [0] * 4
        self.received_count = 0
    def hard_reset(self):
        self.reset()
        self.reset_count = 0
    def respond(self, S):
        self.received_count += 1
        sv = [v for v in self.V if v in S]
        if len(sv) == 0:
            self.return_path_counts[3] += 1
            return 0;
        elif len(sv) >= self.v_remove_count_down:
            if self.random_cut_off == -1:
                self.calls_between[self.calls_between_index] += 1
                return 1
            else:
                if self.random_cut_off > 0:
                    self.random_cut_off -= self.current_decrement
                    return 1
                else:
                    self.return_path_counts[0] += 1
                    return 0
        elif len(sv) == self.v_remove_count_down-1:
            self.v_remove_count_down -= 1
            self.v_remove_count_up += 1
            if self.v_remove_count_up < self.threshold:
                self.calls_between_index += 1
            elif self.v_remove_count_down < self.threshold:
                self.return_path_counts[1] += 1
                return 0
            elif self.v_remove_count_up == self.threshold:
                self.random_cut_off = self.compute_random_cut_off()
                self.current_decrement = 1
            else:
                self.current_decrement = 1
            return 1
        else:
            self.return_path_counts[2] += 1
            return 0
    def compute_random_cut_off(self):
        desired_v_removed = int(gauss(len(V) / 2, len(V)/6))
        while desired_v_removed >= len(V) - self.threshold or desired_v_removed < self.threshold:
            desired_v_removed = int(gauss(len(V) / 2, len(V)/6))
        ratio = 1
        estimate_block_size = int(((len(self.L) - sum(self.calls_between)) / (len(self.V) - self.threshold)) * ratio)
        return estimate_block_size * desired_v_removed + randint(0, estimate_block_size) - self.received_count

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
        while self.shout_group.respond(S) == 1:
            S = S[1:]
        self.set_of_sets.append(S)
        a = self.shout_group.v_remove_count_up
        if a in self.number_of_v_removed:
            self.number_of_v_removed[a] += 1
        else:
            self.number_of_v_removed[a] = 1
        for i in xrange(len(self.cumulative_paths)):
            self.cumulative_paths[i] += self.shout_group.return_path_counts[i]
        self.shout_group.reset()
    def tally(self):
        self.counts = [0] * len(L)
        for s in self.set_of_sets:
            for address in s:
                self.counts[address] += 1


if __name__ == "__main__":
    L = range(200)
    V = []
    while len(V) < 20:
        c = choice(L)
        if not (c in V):
            V.append(c)
    V.sort()
        
    sg = ShoutGroup(L, V, 3)
    ad = Adversary(L, sg)
    iterations = 10000
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
    print cut_off_pos
    plt.bar([x-0.5 for x in xrange(len(V) + 1)], cut_off_pos, width=1, color='g')
    plt.xlim(-0.5,len(V)+0.5)
    plt.xlabel("Number of addresses removed from V during attack")
    plt.ylabel("Times this number of addresses was removed")
    
    plt.show()
    raw_input()
    
    
