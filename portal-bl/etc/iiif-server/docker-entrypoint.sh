#!/bin/bash
set -e

HTML_DIR="/var/www/html"
ORIGINALS_DIR="/var/www/originals"
PYRAMIDS_DIR="/var/www/pyramids"
EBOOKS_DIR="/var/www/ebooks"
IIPSRV_FILE="/var/www/pyramids/iipsrv.fcgi"
IIPSRV_BIN="/usr/lib/iipimage-server/iipsrv.fcgi"

echo 'initializing /var/www/'
if [ -d "$HTML_DIR" ]; then
  echo 'removing unnecessary html folder'
  rm -rf "$HTML_DIR"
fi
for d in "$ORIGINALS_DIR" "$EBOOKS_DIR" "$PYRAMIDS_DIR"; do
  if [ ! -d "$d" ]; then
    echo "creating $d"
    mkdir -p "$d"
  fi
done

# -L: symlink exists (even if broken), -e: target exists
if [ ! -L "$IIPSRV_FILE" ] || [ ! -e "$IIPSRV_FILE" ]; then
  echo 'configuring iipsrv.fcgi script'
  ln -sf "$IIPSRV_BIN" "$IIPSRV_FILE"
fi

# Run Apache as whatever UID/GID the container was started with.
# '#<id>' is Apache's syntax for numeric IDs.
export APACHE_RUN_USER="#$(id -u)"
export APACHE_RUN_GROUP="#$(id -g)"

# Runtime dirs must be writable by the current user
mkdir -p /tmp/apache2/run /tmp/apache2/lock /tmp/apache2/fcgid
export APACHE_RUN_DIR=/tmp/apache2/run
export APACHE_PID_FILE=/tmp/apache2/run/apache2.pid
export APACHE_LOCK_DIR=/tmp/apache2/lock

# Remove a stale PID file left behind by a killed container
rm -f "$APACHE_PID_FILE"

# Load the remaining Debian defaults (APACHE_LOG_DIR etc.).
# Our exports above take precedence thanks to the sed in the Dockerfile.
. /etc/apache2/envvars

# Check the config, then start Apache directly instead of through apache2ctl,
# whose chown calls can't handle numeric '#uid' users.
/usr/sbin/apache2 -t
echo "running apache as uid=$(id -u) gid=$(id -g)"
exec /usr/sbin/apache2 -DFOREGROUND