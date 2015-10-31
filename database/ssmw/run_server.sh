#!/bin/sh
export CREATION=$OPENSHIFT_REPO_DIR/database/create/
export MIGRATION=$OPENSHIFT_REPO_DIR/database/migrate/
export REQUIRED_VER=OUTPUT="$(cat MIGRATION=$OPENSHIFT_REPO_DIR/database/version.properties)"
export PGLOGIN=$OPENSHIFT_POSTGRESQL_DB_USERNAME
export GIVEN_VER="$(psql -c 'copy (select 1) to stdout;' -q -U $PGLOGIN)"

python $OPENSHIFT_REPO_DIR/database/ssmw/ssmw.py $CREATION $MIGRATION $GIVEN_VER $REQUIRED_VER | while read line ; do echo psql -q -U $PGLOGIN -f $line ; done 
