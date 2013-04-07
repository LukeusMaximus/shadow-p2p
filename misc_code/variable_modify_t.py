from random import choice, randint, shuffle, gauss
from stats import binom_stats
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
            if self.v_remove_count_down < self.threshold:
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
        desired_v_removed = int(gauss(len(self.V) / 2, len(self.V)/6))
        while desired_v_removed >= len(self.V) - self.threshold or desired_v_removed < self.threshold:
            desired_v_removed = int(gauss(len(self.V) / 2, len(self.V)/6))
        ratio = 1
        estimate_block_size = int(((len(self.L) - self.received_count) / (len(self.V) - self.threshold)) * ratio)
        return estimate_block_size * desired_v_removed + randint(0, estimate_block_size) - self.received_count

class Adversary:
    def __init__(self, L, sg):
        self.L = L
        self.set_of_sets = []
        self.shout_group = sg
        self.number_of_v_removed = {}
        self.cumulative_paths = [0] * 4
    def attack(self):
        shuffle(self.L)
        S = list(self.L)
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
        self.counts = [0] * len(self.L)
        for s in self.set_of_sets:
            for address in s:
                self.counts[address] += 1

class Attack:
    def __init__(self, l, v, t):
        self.L = range(l)
        self.V = []
        while len(self.V) < v:
            c = choice(self.L)
            if not (c in self.V):
                self.V.append(c)
        self.V.sort()
        self.sg = ShoutGroup(self.L, self.V, t)
        self.ad = Adversary(self.L, self.sg)
    def attack(self, iterations):
        for i in xrange(iterations):
            self.ad.attack()
        self.ad.tally()
        
        self.sorted_tally = [(0,0)] * len(self.ad.counts)
        for i in xrange(len(self.ad.counts)):
            a = 0
            if i in self.V:
                a = 1
            self.sorted_tally[i] = (i, self.ad.counts[i], a)        
        self.sorted_tally = sorted(self.sorted_tally, key=lambda x: x[1])
        
        self.cut_off_pos = [0] * (len(self.V)+1)
        for a in self.ad.number_of_v_removed:
            self.cut_off_pos[a] = self.ad.number_of_v_removed[a]


if __name__ == "__main__":
    increment = 1
    iterations = 18
    average_over = 10
    
    iter_nums = [x*increment + increment for x in xrange(iterations)]
    p_results = [0] * iterations
    for j in xrange(average_over):
        for i in xrange(iterations):
            print j, i
            a = Attack(200, 40, iter_nums[i])
            a.attack(100)
            p_vals = binom_stats(a.sorted_tally, 100)
            p_results[i] += sum(p_vals)/len(p_vals)
    p_results = [x / average_over for x in p_results]
    
    plt.plot(iter_nums, p_results);
    plt.xlabel("Threshold")
    plt.ylabel("p value")
    plt.show()    
       
    raw_input()
    
    
