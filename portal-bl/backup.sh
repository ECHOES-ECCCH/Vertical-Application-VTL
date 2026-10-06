#!/usr/bin/env bash
# To start backup every sunday at 3:30 am add job to cron
# Type in terminal:
# crontab -e
# Choose your favorite editor and add following line to the file:
# 30 3 * * 0 /path/to/backup.sh
# Save.
# Dumps will be saved in $HOME directory.
#
# Info:
# * * * * * command to be executed
# - - - - -
# | | | | |
# | | | | ----- Day of week (0 - 7) (Sunday=0 or 7)
# | | | ------- Month (1 - 12)
# | | --------- Day of month (1 - 31)
# | ----------- Hour (0 - 23)
# ------------- Minute (0 - 59)
set -e
docker exec -t wlt-postgresql pg_dumpall -c -U wlt_portal > dump_`date +%d-%m-%Y"_"%H_%M_%S`.sql