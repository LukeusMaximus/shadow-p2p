from calc_entropy import calc_entropy
from primes import primes

# Iterative Algorithm (xgcd)
def iterative_egcd(a, b):
    x,y, u,v = 0,1, 1,0
    while a != 0:
        q,r = b/a,b%a; m,n = x-u*q,y-v*q # use x//y for floor "floor division"
        b,a, x,y, u,v = a,r, u,v, m,n        
    return b, x, y

grid_size = 8

def get_grid((x,y)):
    return y * grid_size + x

def get_right((x,y)):
    return ((x + 1) % grid_size, y)
    
def get_left((x,y)):
    return ((x - 1) % grid_size, y)
    
def get_up((x,y)):
    return (x, (y + 1) % grid_size)
    
def get_down((x,y)):
    return (x, (y - 1) % grid_size)

def get_distribution(point, receiver_name, steps):
    if steps == 0:
        if receiver_name != get_grid(point):
            return {get_grid(point) : 1.0}
        else:
            return {}
    else:
        left_dist = get_distribution(get_left(point), receiver_name, steps-1)
        down_dist = get_distribution(get_down(point), receiver_name, steps-1)
        collect = dict(left_dist)
        for x in down_dist:
            if x in collect:
                collect[x] += down_dist[x]
            else:
                collect[x] = down_dist[x]
        this_letter = get_grid(point)
        if this_letter != receiver_name:
            if this_letter in collect:
                collect[this_letter] += 1.0
            else:
                collect[this_letter] = 1.0
        collect_sum = 0
        for x in collect:
            collect_sum += collect[x]
        for x in collect:
            collect[x] /= collect_sum
        return collect
        
def get_distribution2(point, excludes, steps_x, steps_y, results):
    this_letter = get_grid(point)
    if this_letter in results:
        return results[this_letter]
    else:
        collect = {}
        divisor = 0
        if steps_x != 0:
            left_dist = get_distribution2(get_left(point), excludes, steps_x-1, steps_y, results)
            collect.update(left_dist)
            divisor += 1
        if steps_y != 0:
            down_dist = get_distribution2(get_down(point), excludes, steps_x, steps_y-1, results)
            divisor += 1
            for x in down_dist:
                if x in collect:
                    n,d = collect[x]
                    n2,d2 = down_dist[x]
                    if d % d2 == 0:
                        collect[x] = (n + n2 * (d/d2),  d)
                    elif d2 % d == 0:
                        collect[x] = (n * (d2/d) + n2,  d2)
                    else:
                        collect[x] = (n * d2 + n2 * d,  d * d2)
                else:
                    collect[x] = down_dist[x]
            
        if not (this_letter in excludes):
            divisor += 1
            if this_letter in collect:
                n,d = collect[this_letter]
                collect[this_letter] = (n+d,d)
            else:
                collect[this_letter] = (1,1)
        
        if divisor > 0:
            for x in collect:
                n,d = collect[x]
                collect[x] = (n,d*divisor)
                
        results[this_letter] = collect
        return collect
    
def nice_denominator(dist):
    #First, simplify
    dist2 = {}
    for x in dist:
        n,d = dist[x]
        gcd, _, _ = iterative_egcd(n,d)
        dist2[x] = (n/gcd,d/gcd)

    lcm_accum = 1
    denominators = [dist2[x][1] for x in dist2]
    i = 0
    while(sum(denominators) != len(denominators)):
        has_divided = False
        first = True
        while(first or has_divided):
            has_divided = False
            first = False
            for j in xrange(len(denominators)):
                if(denominators[j] % primes[i] == 0):
                    denominators[j] /= primes[i]
                    has_divided = True
            if has_divided:
                lcm_accum *= primes[i]
        i += 1
    denominator = lcm_accum
    for x in dist2:
        n,d = dist2[x]
        if d < denominator:
            dist2[x] = (n * (denominator/d), denominator)
        elif d > denominator:
            dist2[x] = (n * (d / denominator), denominator)
    return dist2
    
def distribution_entropy(dist):
    arr = [float(dist[x][0]) / dist[x][1] for x in dist]
    entropy = calc_entropy(arr)
    percentage = (entropy * 100) / calc_entropy([1.0/len(arr) for i in xrange(len(arr))])
    return entropy, percentage   

if __name__ == "__main__":
    receiver = (0,0)
    grid_size = 2
    while grid_size <= 64:
        dist = get_distribution2(receiver, [get_grid(receiver)], grid_size-1, grid_size-1, {})
        print grid_size, distribution_entropy(dist)
        grid_size *= 2
        
    grid_size = 2
    while grid_size <= 64:
        dist = get_distribution2(receiver, [get_grid((grid_size-1,grid_size-1))], grid_size-1, grid_size-1, {})
        print grid_size, distribution_entropy(dist)
        grid_size *= 2
    
