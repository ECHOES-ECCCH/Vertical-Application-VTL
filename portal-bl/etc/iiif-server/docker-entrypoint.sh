#!/bin/bash

HTML_DIR="/var/www/html"

ORIGINALS_DIR="/var/www/originals"
PYRAMIDS_DIR="/var/www/pyramids"
EBOOKS_DIR="/var/www/ebooks"
IIPSRV_FILE="/var/www/pyramids/iipsrv.fcgi"

echo 'initializing /var/www/'
if [ -d $HTML_DIR ]; then
  echo 'removing unnecessary html folder'
  rm -rf /var/www/html
fi
if [ ! -d $ORIGINALS_DIR ]; then
  echo 'creating originals folder'
  mkdir /var/www/originals
fi
if [ ! -d $EBOOKS_DIR ]; then
  echo 'creating ebooks folder'
  mkdir /var/www/ebooks
fi
if [ ! -d $PYRAMIDS_DIR ]; then
  echo 'creating pyramids folder'
  mkdir /var/www/pyramids
fi
if [ ! -d $IIPSRV_FILE ]; then
  echo 'configuring iipsrv.fcgi script'
  ln -s /usr/lib/iipimage-server/iipsrv.fcgi /var/www/pyramids/
fi

echo 'running apache'
apache2ctl -DFOREGROUND
tail -n 20 /var/log/apache2/error.log
