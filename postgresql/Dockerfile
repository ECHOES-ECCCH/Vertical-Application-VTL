FROM postgres:15.3

COPY tsearch_data/* /usr/share/postgresql/15/tsearch_data/
COPY docker-entrypoint-initdb.d/* /docker-entrypoint-initdb.d/
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
