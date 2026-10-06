#!/usr/bin/env bash
set -e
docker-compose down
docker-compose up -d $@
docker cp vtl-storage/ wlt-iiif-tools:/var/
test -t 1 && USE_TTY="t"
docker exec -i${USE_TTY} wlt-iiif-server chmod -R 755 /var/www/pyramids
docker exec -i${USE_TTY} wlt-iiif-server chmod -R 744 /var/www/originals
