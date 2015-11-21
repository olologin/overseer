#!/usr/bin/python
# -*- coding: utf-8 -*-
import logging as log
import os
import sys
import tempfile
import subprocess


def _bfs_paths(graph, start, goal):
    """
    :param graph: Adjacency dictionary, graph["3"]["2"] contains path to migration sql script
    if there is any
    :param start: starting point of bfs, for example start = "1" means that we want to find
      all possible paths "1" to goal through migration scripts, if there is no version at the
       moment - start should be None.
    :param goal: Desired version, for example "7"
    :return: Yields list of verticles on path from start to goal, according to adjacency from
    graph dictionary, least length path yields earlier than longer path
    """
    queue = [(start, [start])]
    while queue:
        (vertex, path) = queue.pop(0)
        if vertex not in graph:
            continue
        remaining_paths = set(graph[vertex].keys()) - set(path)
        for next_value in remaining_paths:
            if next_value == goal:
                yield path + [next_value]
            else:
                queue.append((next_value, path + [next_value]))


def _convert2filelist(graph, path):
    """
    Converts list of verticles to list of file paths (migrations, creation sql scripts)
    :param graph: same as graph parameter in _bfs_paths
    :param path: list of verticles which determines particular path through verticles
                 for example ["3", "4", "5", "10"]
    :return: List of creation/migration sql scripts, according to graph values at corresponding edges
    """
    return [graph[path[i]][path[i+1]] for i in range(len(path)-1)]


def _build_graph(creation_path, migration_path):
    """
    Creates graph (adjacency dictionary) for verticles from paths with creation and migration scripts
    Each verticle in this graph - version, you can reach all creation scripts from None verticle (if
    there is no version at the moment), i.e. graph[None]["1"] points on creation script for ver 1
    1.sql graph["1"]["3"] points on migration script 1to3.sql
    :param creation_path: path to creation folder
    :param migration_path: path to migration folder
    :return: adjacency dict for all found versions (verticles)
    """
    result = {None:{}}
    for root, dirs, fileset in os.walk(creation_path):
        for next_file in fileset:
            if next_file.endswith(".sql"):
                full_path = os.path.join(root, next_file)
                b = next_file.replace(".sql", "")
                result[None][b] = full_path

    for root, dirs, fileset in os.walk(migration_path):
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


def shortest_path(creation_path, migration_path, start, goal):
    """
    :param creation_path: path to creation folder
    :param migration_path: path to migration folder
    :param start: starting point of bfs, for example start = "1" means that we want to find
      all possible paths from version "1" to goal through migration scripts, if there is no
      version at the moment - start should be None.
    :param goal: Desired version, for example "7"
    :return: yields list of filepaths, each filepath in this list could point on creation sql
    script, or on migration script.
    """
    graph = _build_graph(creation_path, migration_path)
    if start == goal:
        log.info("Scheme version is %s already, skipping\n" % (start))
        return
    for path in _bfs_paths(graph, start, goal):
        log.info("Yielding list: %s\n" % (path))
        file_list = _convert2filelist(graph, path)
        yield file_list
    log.error("There is no any other " +
                     "possible paths from %s to %s!\n" % (start, goal))


class Psql:
    def __init__(self, db_exec = "psql", db_pass=None, params = ""):
        self.password = db_pass
        self.params   = self._string_param_to_list(params)
        self.psql     = db_exec

    def _string_param_to_list(self, string_param):
        return string_param.split(" ") if string_param != "" else []

    def execute_query(self, query):
        try:
            call = [self.psql] + query + self.params
            log.info("Executing command %s"%call)
            stdout = subprocess.check_output(call,
                                             env=None if db_pass is None else {"PGPASSWORD":self.password})
            return 0, stdout
        except subprocess.CalledProcessError as e:
            return e.returncode, e.output

    def get_db_version(self, db_dbname):
        errcode, output = self.execute_query(
            ["-d", db_dbname, "-qc", "copy (select max(id) from version) to stdout"])

        if errcode == 0:
            return output
        else:
            log.error("There is no db scheme at the moment")
            return None

    def is_db_exists(self, db_dbname):
        errcode, output = self.execute_query(["-lqtA"])
        if errcode != 0:
            # TODO Throw some error
            return False

        for line in output.splitlines():
            dbname_ = line.split("|")[0] # Get table name exactly
            if dbname_ == db_dbname:
                return True
        return False



# The actual code starts here
if __name__ == "__main__":
    log.basicConfig(level=log.DEBUG)

    db_params = os.environ.get("DB_PARAMS")
    db_pass   = os.environ.get("DB_PASS")
    try:
        db_exec   = os.environ["DB_EXEC"]
        db_dbname = os.environ["DB_DBNAME"]
        db_create = os.environ["DB_CREATE"]
        db_migrate= os.environ["DB_MIGRATE"]
        db_required_scheme = os.environ["DB_REQUIRED_SCHEME"]
    except KeyError as e:
        log.error("You haven't provided %s environment variable."%e.args[0])
        sys.exit(78)


    instance = Psql(db_exec, db_pass, db_params)

    current = None
    if instance.is_db_exists(db_dbname):
        current = instance.get_db_version(db_dbname)
    for filelist in shortest_path(db_create, db_migrate, current, db_required_scheme):
        fd, merged_sql = tempfile.mkstemp(text=True, suffix=".sql")
        log.info("Merging %s list of files into file %s"%(filelist, merged_sql))
        tmpf = os.fdopen(fd, 'w')
        for file in filelist:
            with open(file) as f:
                tmpf.write("\n -- %s\n"%file)
                tmpf.writelines(f.readlines())
        tmpf.write("\n")
        tmpf.close()
        log.info("Executing file %s as single transaction", merged_sql)
        err, output = instance.execute_query(["--single-transaction", "-f", merged_sql])
        if err == 0 and instance.get_db_version(db_dbname) == db_required_scheme:
            log.info("migration completed!")
            os.remove(merged_sql)
            break