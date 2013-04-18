import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
from matplotlib import cm
from random import randint, shuffle

def add_directed_traffic(grid, (x1,y1), (x2,y2), iterations):
    width = len(grid)
    dx, dy = x2-x1, y2-y1
    first = True
    for i in xrange(iterations):
        x, y = x1, y1
        route = [0] * dx + [1] * dy
        if first:
            print route, len(route), dx, dy
            first = False
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

if __name__ == "__main__":
    width = 16
    grid = []
    for i in xrange(width):
        grid.append([0] * width)
    add_random_traffic(grid, 10000)
    add_directed_traffic(grid, (3,3), (13,13), 1000)
    for i in xrange(len(grid)-1, -1, -1):
        print grid[i]
    plot_grid(grid)
    raw_input()
