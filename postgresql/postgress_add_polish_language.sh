#!/bin/bash

su -c /usr/lib/postgresql/9.6/bin/postgres postgres &

add_polish(){
	echo "ADDING POLISH FULL TEXT SEARCH"
	dbhost=localhost
	dbport=5432
	dbuser=$POSTGRES_USER
	dbpass=$POSTGRES_PASSWORD
	dbname=$POSTGRES_DB
	export PGPASSWORD="$dbpass"
	dbopts="-h $dbhost -p $dbport -U $dbuser -d $dbname"

	query="
	CREATE TEXT SEARCH DICTIONARY polish_ispell (
	  Template = ispell,
	  DictFile = polish,
	  AffFile = polish,
	  StopWords = polish
	);

	CREATE TEXT SEARCH CONFIGURATION polish(parser = default);

	ALTER TEXT SEARCH CONFIGURATION polish
	  ALTER MAPPING FOR asciiword, asciihword, hword_asciipart, word, hword, hword_part
	  WITH polish_ispell;
	"

	psql $dbopts <<< "$query"
	touch polish_add_done
}

check_db(){
        dbhost=localhost
        dbport=5432
        dbuser=$POSTGRES_USER
        dbpass=$POSTGRES_PASSWORD
        dbname=$POSTGRES_DB
        export PGPASSWORD="$dbpass"
        dbopts="-h $dbhost -p $dbport -U $dbuser -d $dbname"

        query="
		SELECT 1;
        "
        psql $dbopts <<< "$query" > /dev/null 2>&1

	retVal=$?
	if [ $retVal -eq 0 ]; then
	    return 1
	fi
	return 0
}


if [ ! -f polish_add_done ]; then
        while check_db; do sleep 1; done;
          add_polish;
fi
fg
