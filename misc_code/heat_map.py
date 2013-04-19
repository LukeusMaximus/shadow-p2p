import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
from matplotlib import cm
from random import randint, shuffle
from scipy.stats import binom_test

def add_clever_traffic(grid, temp_grid, (x1,y1), (x2,y2), iterations):
    width = len(grid)
    dx, dy = x2-x1, y2-y1
    for i in xrange(iterations):
        x, y = x1, y1
        route = []
        while len(route) < dx + dy:
            tx, ty = -1, -1
            if x < x2:
                tx = temp_grid[y][x+1]
            if y < y2:
                ty = temp_grid[y+1][x]
            if tx == -1 and ty == -1:
                d = randint(0, 1)
                route.append(d)
                if d == 0:
                    temp_grid[y][x+1] += 1
                    x += 1
                else:
                    temp_grid[y+1][x] += 1
                    y += 1
            elif tx == -1 and ty != -1:
                route.append(1)
                temp_grid[y+1][x] += 1
                y += 1
            elif ty == -1 and tx != -1:
                route.append(0)
                temp_grid[y][x+1] += 1
                x += 1
            elif ty < tx:
                route.append(1)
                temp_grid[y+1][x] += 1
                y += 1
            elif tx < ty:
                route.append(0)
                temp_grid[y][x+1] += 1
                x += 1
            elif tx == ty:
                d = randint(0, 1)
                route.append(d)
                if d == 0:
                    temp_grid[y][x+1] += 1
                    x += 1
                else:
                    temp_grid[y+1][x] += 1
                    y += 1
        x, y = x1, y1
        for i in xrange(len(route)):
            if route[i] == 0:
                x = (x + 1) % width
            else:
                y = (y + 1) % width
            grid[y][x] += 1
        
    
    

def add_directed_traffic(grid, (x1,y1), (x2,y2), iterations):
    width = len(grid)
    dx, dy = x2-x1, y2-y1
    for i in xrange(iterations):
        x, y = x1, y1
        route = [0] * dx + [1] * dy
        shuffle(route)
        for i in xrange(len(route)):
            if route[i] == 0:
                x = (x + 1) % width
            else:
                y = (y + 1) % width
            grid[y][x] += 1
    

def add_random_traffic(grid, iterations):
    width = len(grid)
    max_route_len = 2 * (width -1)
    for i in xrange(iterations):
        x, y = randint(0, width-1), randint(0, width-1)
        route = [0] * randint(1, max_route_len)
        for i in xrange(len(route)):
            route[i] = randint(0,1)
        for i in xrange(len(route)):
            if route[i] == 0:
                x = (x + 1) % width
            else:
                y = (y + 1) % width
            grid[y][x] += 1
        

def plot_grid(grid):
    width = len(grid)
    fig = plt.figure()
    ax = fig.add_subplot(111, projection='3d')
    X = [range(0,width)] * width
    Y = []
    for i in xrange(width):
        Y.append([i] * width)
    ax.plot_surface(X, Y, grid, cmap=cm.coolwarm, cstride=1, rstride=1, linewidth=0.1)
    ax.set_xlabel("X coordinate")
    ax.set_ylabel("Y coordinate")
    ax.set_zlabel("Number of packets received")
    plt.show()

def p_values(grid, (x1,y1), (x2,y2), background_traffic, directed_traffic):
    width = len(grid)
    p = (width - 1.0) / (width * width)
    n = background_traffic + directed_traffic
    
    '''
    ordered_info = []
    for y in xrange(width):
        for x in xrange(width):
            ordered_info.append((x,y,grid[y][x]))
    ordered_info = sorted(ordered_info, key=lambda x: x[2])
    p_values = []
    for i in xrange(len(ordered_info)):
        p_values.append(binom_test(ordered_info[i][2], n, p))
    print p_values
    '''
    
    p_value_grid = []
    for y in xrange(width):
        row = []
        for x in xrange(width):
            row.append(binom_test(grid[y][x], n, p))
        p_value_grid.append(row)
    return p_value_grid
    
def has_significant_path(p_value_grid, (x1,y1), (x2,y2), sig_level):
    width = len(p_value_grid)
    bool_grid = []
    for i in xrange(width):
        row = []
        for j in xrange(width):
            v = False
            if p_value_grid[i][j] < sig_level:
                v = True
            row.append(v)
        bool_grid.append(row)
    
    reach_grid = []
    for i in xrange(width):
        reach_grid.append([False] * width)
    reach_grid[y1][x1] = True
    
    s = 1
    t = 0
    while t != s:
        t = s
        for y in xrange(width):
            for x in xrange(width):
                if bool_grid[y][x] and not reach_grid[y][x]:
                    flip = False
                    if x < width-1 and reach_grid[y][x+1]:
                        flip = True
                    if x > 0 and reach_grid[y][x-1]:
                        flip = True
                    if y < width-1 and reach_grid[y+1][x]:
                        flip = True
                    if y > 0 and reach_grid[y-1][x]:
                        flip = True
                    if flip:
                        reach_grid[y][x] = True
                        s += 1
                        if x == x2 and y == y2:
                            return True
    return False
    
    
def plot_p_values(p_value_grid):
    width = len(p_value_grid)
    fig = plt.figure()
    ax = fig.add_subplot(111, projection='3d')
    X = [range(0,width)] * width
    Y = []
    for i in xrange(width):
        Y.append([i] * width)
    ax.plot_surface(X, Y, p_value_grid, cmap=cm.coolwarm, cstride=1, rstride=1, linewidth=0.1)
    ax.set_xlabel("X coordinate")
    ax.set_ylabel("Y coordinate")
    ax.set_zlabel("p-value")
    plt.show()
    

if __name__ == "__main__":
    width = 16
    background_traffic = 10000
    directed_traffic = 100
    increment = 100
    start = (3,3)
    end = (13,13)
    
    grid = []
    for i in xrange(width):
        grid.append([0] * width)
        
    temp_grid = []
    for i in xrange(width):
        temp_grid.append([0]*width)
        
    add_random_traffic(grid, background_traffic)
    #add_directed_traffic(grid, start, end, directed_traffic)
    add_clever_traffic(grid, temp_grid, start, end, increment)
        
    cont = True;
    while cont:
        '''
        for i in xrange(len(grid)-1, -1, -1):
            print grid[i]
        plot_grid(grid)
        '''
        p_vals = p_values(grid, start, end, background_traffic, directed_traffic)
        a = has_significant_path(p_vals, start, end, 0.05)
        b = has_significant_path(p_vals, start, end, 0.01)
        print directed_traffic, 0.05, a, 0.01, b
        
        if b:
            cont = False
            plot_p_values(p_vals)
        else:
            directed_traffic += increment
            #add_directed_traffic(grid, start, end, increment)
            add_clever_traffic(grid, temp_grid, start, end, increment)
    
    
    
