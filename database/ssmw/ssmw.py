#!/usr/bin/python
# -*- coding: utf-8 -*-

"""
Authors: https://github.com/hdghg
         https://github.com/olologin

This script is a migration tool for PostgreSQL databases, it was
written in pure Python, and should be compatible with 2 and 3 branches of Python

The only required dependency to run it - psql utility from standart PostgreSQL
client distribution. This script gets all necessary params from environment
variables, thus you should specify this parameters:
    DB_EXEC: default - psql
    DB_DBNAME: name of database which you want to upgrade
    DB_CREATE: path to directory which contains all creation scripts
    each creation script should have name %d.sql where %d - version
    of db, for example 1.sql, 5.sql, etc
    DB_MIGRATE: path to directory which contains all migration scripts
    each creation script should have name %dto%d.sql where particular %d - version
    of db, for example 1to3.sql, 5to10.sql
    DB_REQUIRED_SCHEME: Required scheme version
unnecessary params:
    DB_PARAMS: Additional custom parameters, like -U
    DB_PASS: Password to database

This script will try to build all possible paths from existing (or non existing) scheme
and merge creation (if needed) with all needed migrations into one temporary file, which
then it will execute in a single transaction, so your database will be completely upgraded
to required scheme version, or will remain on current version if correct upgrade is impossible

This script terminates with following return codes:
    OK = 0
    PARAM_ERR = 78
    DB_AUTH_ERR = 77
    DB_NOT_EXISTS = 66
    MISC_DB_ERR = 70
    WRONG_CREATE_MIGRATE_DIR = 72
    CANNOT_FIND_MIGRATION_PATH = 67
"""

import logging as log
import os
import sys
import tempfile
import subprocess

OK = 0
PARAM_ERR = 78
DB_AUTH_ERR = 77
DB_NOT_EXISTS = 66
MISC_DB_ERR = 70
WRONG_CREATE_MIGRATE_DIR = 72
CANNOT_FIND_MIGRATION_PATH = 67

def _bfs_paths(graph, start, goal):
    """
    :param graph: Adjacency dictionary, graph["3"]["2"] contains path to
    a migration sql script if there is any
    :param start: starting point of bfs, for example start = "1" means that
    we want to find all possible paths "1" to goal through migration scripts,
    if there is no version at the moment - start should be None.
    :param goal: Desired version, for example "7"
    :return: Yields list of verticles on path from start to goal, according to
    adjacency from graph dictionary, least length path yields earlier than
    longer path
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
    Converts list of verticles to list of file paths (migrations, creation sql
    scripts)
    :param graph: same as graph parameter in _bfs_paths
    :param path: list of verticles which determines particular path through
    verticles for example ["3", "4", "5", "10"]
    :return: List of creation/migration sql scripts, according to graph values
    at corresponding edges
    """
    return [graph[path[i]][path[i+1]] for i in range(len(path)-1)]


def _build_graph(creation_path, migration_path):
    """
    Creates graph (adjacency dictionary) for verticles from paths with creation
    and migration scripts Each verticle in this graph - version, you can reach
    all creation scripts from None verticle (if there is no version at the
    moment), i.e. graph[None]["1"] points on creation script for ver 1 1.sql
    graph["1"]["3"] points on migration script 1to3.sql
    :param creation_path: path to creation folder
    :param migration_path: path to migration folder
    :return: adjacency dict for all found versions (verticles)
    """
    result = {None: {}}
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
    :param start: starting point of bfs, for example start = "1" means that we
    want to find all possible paths from version "1" to goal through migration
    scripts, if there is no version at the moment - start should be None.
    :param goal: Desired version, for example "7"
    :return: yields list of filepaths, each filepath in this list could point
    on creation sql script, or on migration script.
    """
    graph = _build_graph(creation_path, migration_path)
    if start == goal:
        return
    for path in _bfs_paths(graph, start, goal):
        file_list = _convert2filelist(graph, path)
        yield file_list
    raise PathException("There is no any other " +
                        "possible paths from %s to %s!\n" % (start, goal))


class Psql:
    def __init__(self, db_exec="psql", db_pass=None, params=None):
        self.password = db_pass
        self.params = self._string_param_to_list(params)
        self.psql = db_exec

    def _string_param_to_list(self, string_param):
        if string_param != "" and string_param is not None:
            return string_param.split(" ")
        else:
            return []

    def execute_query(self, query):
        call = [self.psql] + query + self.params
        log.debug("Executing command %s" % call)
        env_ = None if db_pass is None else {"PGPASSWORD": self.password}
        p = subprocess.Popen(call,
                             env=env_,
                             universal_newlines=True,
                             stdout=subprocess.PIPE,
                             stderr=subprocess.PIPE)
        stdout, stderr = p.communicate()
        returncode = p.returncode

        if stderr != "":
            raise DBException(stderr, returncode)
        return stdout

    def get_db_version(self, db_dbname):
        try:
            output = self.execute_query(
                ["-d",
                 db_dbname,
                 "-qc",
                 "copy (select max(id) from version) to stdout"
                 ]
            )
            return output
        except DBException as exc:
            if 'ERROR:  relation "version" does not exist' in exc.message and exc.returncode == 1:
                return None
            # Unknown exception, rethrow
            raise

    def is_db_exists(self, db_dbname):
        output = self.execute_query(["-lqtA"])
        for line in output.splitlines():
            dbname_ = line.split("|")[0]  # Get table name exactly
            if dbname_ == db_dbname:
                return True
        return False


class DBException(Exception):
    def __init__(self, message, returncode):
        super(DBException, self).__init__(message)
        self.returncode = returncode


class PathException(Exception):
    pass

# The actual code starts here
if __name__ == "__main__":
    log.basicConfig(level=log.DEBUG)

    db_params = os.environ.get("DB_PARAMS")
    db_pass = os.environ.get("DB_PASS")
    try:
        db_exec = os.environ["DB_EXEC"]
        db_dbname = os.environ["DB_DBNAME"]
        db_create = os.environ["DB_CREATE"]
        db_migrate = os.environ["DB_MIGRATE"]
        db_required_scheme = os.environ["DB_REQUIRED_SCHEME"]
    except KeyError as e:
        log.error("You haven't provided %s environment variable." % e.args[0])
        sys.exit(PARAM_ERR)

    log.debug("""Script was started with following parameters:
                DB_PARAMS: %s
                DB_PASS: %s
                DB_EXEC: %s
                DB_DBNAME: %s
                DB_CREATE: %s
                DB_MIGRATE: %s
                DB_REQUIRED_SCHEME: %s
                """ % (db_params, db_pass, db_exec, db_dbname,
                       db_create, db_migrate, db_required_scheme)
              )

    instance = Psql(db_exec, db_pass, db_params)

    current = None
    try:
        if instance.is_db_exists(db_dbname):
            current = instance.get_db_version(db_dbname)
            if current is None:
                log.info("There is no db version at all")
        else:
            log.error("There is no db at the moment")
            sys.exit(DB_NOT_EXISTS)

        if not (os.path.isdir(db_create) and os.path.isdir(db_migrate)):
            log.error("Wrong creation or migration path")
            sys.exit(WRONG_CREATE_MIGRATE_DIR)

        for filelist in shortest_path(db_create, db_migrate, current, db_required_scheme):
            fd, merged_sql = tempfile.mkstemp(text=True, suffix=".sql")
            log.info("Merging %s list of files into file %s" % (filelist, merged_sql))
            tmpf = os.fdopen(fd, 'w')
            for file in filelist:
                with open(file) as f:
                    tmpf.write("\n -- %s\n" % file)
                    tmpf.writelines(f.readlines())
            tmpf.write("\n")
            tmpf.close()
            log.info("Executing file %s as single transaction", merged_sql)
            instance.execute_query(["--single-transaction", "-f", merged_sql])
            if instance.get_db_version(db_dbname) == db_required_scheme:
                log.info("migration completed!")
                os.remove(merged_sql)
                break
        log.info("Scheme version is %s already, skipping\n" % current)
        sys.exit(OK)
    except DBException as exc:
        log.error(exc.message)
        if exc.returncode == 2 and 'FATAL:  password authentication failed for user' in exc.message:
            sys.exit(DB_AUTH_ERR)
        else:
            sys.exit(MISC_DB_ERR)
    except PathException as exc:
        log.error(exc.message)
        sys.exit(CANNOT_FIND_MIGRATION_PATH)
