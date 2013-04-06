from random import choice, randint, shuffle, gauss
from stats import graphs

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
        self.return_path_counts = [0] * 4
        self.received_count = 0
        self.desired_v_removed = self.select_v_remove_lim()
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
                return 1
            elif self.random_cut_off > 0:
                self.random_cut_off -= self.current_decrement
                return 1
            else:
                self.return_path_counts[0] += 1
                return 0
        elif len(sv) == self.v_remove_count_down-1:
            self.v_remove_count_down -= 1
            self.v_remove_count_up += 1
            if self.v_remove_count_down < self.threshold:
                self.return_path_counts[1] += 1
                return 0
            elif self.v_remove_count_up == self.desired_v_removed:
                self.random_cut_off = self.compute_random_cut_off()
                if self.random_cut_off == 0:
                    return 0
                self.current_decrement = 1
            else:
                self.current_decrement = 1
            return 1
        else:
            self.return_path_counts[2] += 1
            return 0
    def select_v_remove_lim(self):
        desired_v_removed = int(gauss(len(V) / 2, len(V)/6))
        while desired_v_removed >= len(V) - self.threshold or desired_v_removed < self.threshold:
            desired_v_removed = int(gauss(len(V) / 2, len(V)/6))
        return desired_v_removed
    def compute_random_cut_off(self):
        ratio = 1
        estimate_block_size = int(((len(self.L) - self.received_count) / len(self.V)) * ratio)
        return randint(0, estimate_block_size)

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
    
    cut_off_pos = [0] * (len(V)+1)
    for a in ad.number_of_v_removed:
        cut_off_pos[a] = ad.number_of_v_removed[a]
        
    graphs(sorted_tally, cut_off_pos)
    raw_input()
    
    
