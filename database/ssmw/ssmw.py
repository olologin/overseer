#!/usr/bin/python
import os
import sys


def bfs_paths(structure, start, goal):
    queue = [(start, [start])]
    while queue:
        (vertex, path) = queue.pop(0)
        if vertex not in structure.keys():
            continue
        remaining_paths = set(dict(structure[vertex]).keys()) - set(path)
        for next_value in remaining_paths:
            if next_value == goal:
                yield path + [next_value]
            else:
                queue.append((next_value, path + [next_value]))


def shortest_path(structure, start, goal):
    if start == goal:
        return set(start)
    try:
        return next(bfs_paths(structure, start, goal))
    except StopIteration:
        return None


def files(structure, start, goal):
    way = shortest_path(structure, start, goal)
    if way is None:
        sys.stderr.write("ERROR: Migration path is unreachable!\n")
    elif len(way) == 1:
        sys.stderr.write("INFO : Already here, skipping\n")
    else:
        previous_value = None
        for next_value in list(way):
            if previous_value is not None:
                yield structure[previous_value][next_value]
            previous_value = next_value


def build_graph(start_path):
    result = {}
    for root, dirs, fileset in os.walk(start_path):
        for next_file in fileset:
            if next_file.endswith(".sql"):
                full_path = os.path.join(root, next_file)
                wire = next_file.replace(".sql", "").split("to")
                a = wire[0]
                b = wire[1]
                if a in result.keys():
                    result[a][b] = full_path
                else:
                    result[a] = {b: full_path}
    return result


# The actual code starts here

all_files = build_graph(sys.argv[2])
for nxt in files(all_files, sys.argv[3], sys.argv[4]):
    print nxt
