import matplotlib.pyplot as plt
from scipy.stats import wilcoxon, binom_test
import scipy.stats as sp
from random import randint

def binom_stats(sorted_tally, n):
    data = [sorted_tally[i] for i in xrange(len(sorted_tally)) if sorted_tally[i][2] == 1]
    return [binom_test(data[i][1], n, 0.5) for i in xrange(len(data))]

def wilcoxon_stats(sorted_tally):
    l = len(sorted_tally)

    in_set = [i for i in xrange(len(sorted_tally)) if sorted_tally[i][2] == 1]
    out_set = []
    while len(out_set) != len(in_set):
        r = randint(0,l-1)
        if not (r in out_set):
            out_set.append(r) 
    in_set.sort()
    out_set.sort()
    z_stat, p_val = wilcoxon(in_set, out_set)
    return z_stat, p_val

def graphs(sorted_tally, cut_off_pos):
    l = len(sorted_tally)
    v = len(cut_off_pos)

    plt.figure(1)
    plt.subplot(211)
    plt.bar(range(l), [x[1] for x in sorted_tally], width = 1, lw=0)
    plt.ylabel("Number of appearances")
    plt.subplot(212)
    plt.scatter(range(l), [x[2] for x in sorted_tally], lw=0)
    plt.xlim(0, l)
    plt.ylabel("Is address in shout group")
    plt.yticks([0,1])
    plt.xlabel("Address occurring in S")
    plt.savefig("fig_" + str(randint(0, 10000)) + ".eps")
    plt.close()
    
    plt.figure(2)
    plt.bar([x-0.5 for x in xrange(v)], cut_off_pos, width=1, color='g')
    plt.xlim(-0.5,v-0.5)
    plt.xlabel("Number of addresses removed from V during attack")
    plt.ylabel("Times this number of addresses was removed")
    plt.savefig("fig_" + str(randint(0, 10000)) + ".eps")
    plt.close()
    
