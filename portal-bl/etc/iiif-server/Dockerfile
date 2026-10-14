FROM debian:trixie-slim AS iipbuild

RUN set -eux; \
    apt-get -y update; \
    apt-get -y install --no-install-recommends \
        autoconf automake autotools-dev libtool make g++ pkg-config \
        ca-certificates curl \
        libfcgi-dev libjpeg-dev libtiff-dev libopenjp2-7-dev \
        libmemcached-dev libwebp-dev libpng-dev libzstd-dev zlib1g-dev; \
    rm -rf /var/lib/apt/lists/*

WORKDIR /usr/src

RUN set -eux; \
    curl -fsSL https://github.com/ruven/iipsrv/archive/refs/tags/iipsrv-1.3.tar.gz | tar xz; \
    mv iipsrv-iipsrv-1.3 iipsrv; \
    cd iipsrv; \
    ./autogen.sh; \
    CXXFLAGS="-O2 -include cstdint" ./configure --enable-openjpeg; \
    make -j"$(nproc)"

FROM debian:trixie-slim AS publish

ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=Europe/Warsaw

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        apache2 libapache2-mod-fcgid \
        libfcgi-bin libjpeg62-turbo libjpeg-turbo-progs libtiff6 libopenjp2-7 \
        libmemcached11t64 libwebp7 libwebpmux3 libpng16-16t64 libzstd1 libgomp1 \
        libvips-tools \
        dmidecode ipmitool nfs-common procps sysstat samba \
    && a2enmod rewrite headers fcgid \
    && apt-get -y clean \
    && rm -rf /var/lib/apt/lists/*

COPY iipsrv.conf /etc/apache2/mods-available/iipsrv.conf

RUN touch /etc/apache2/mods-available/iipsrv.load && \
    a2enmod iipsrv && \
    mkdir /usr/lib/iipimage-server

COPY --from=iipbuild /usr/src/iipsrv/src/iipsrv.fcgi /usr/lib/iipimage-server/

RUN sed -i 's+\tDocumentRoot /var/www/html+\tDocumentRoot /var/www+g' /etc/apache2/sites-available/000-default.conf && \
    sed -i '57i ServerName "iiif-server"' /etc/apache2/apache2.conf && \
    sed -i 's/<VirtualHost \*:80>/<VirtualHost *:8080>/g' /etc/apache2/sites-enabled/000-default.conf && \
    sed -i 's/Listen 80/Listen 8080/g' /etc/apache2/ports.conf && \
    # make envvars values overridable from the entrypoint
    sed -i -E 's/^export (APACHE_RUN_USER|APACHE_RUN_GROUP|APACHE_RUN_DIR|APACHE_PID_FILE|APACHE_LOCK_DIR)=(.*)$/export \1=${\1:-\2}/' /etc/apache2/envvars && \
    # logs to stdout/stderr so `docker logs` shows them and no write perms are needed
    ln -sf /dev/stderr /var/log/apache2/error.log && \
    ln -sf /dev/stdout /var/log/apache2/access.log && \
    ln -sf /dev/stdout /var/log/apache2/other_vhosts_access.log

COPY directory.conf /etc/apache2/sites-enabled/
COPY docker-entrypoint.sh ./

RUN chmod +x ./docker-entrypoint.sh && \
    chgrp -R 0 / 2> /dev/null || true && \
    chmod -R g=u / 2> /dev/null || true

EXPOSE 8080
ENTRYPOINT ["bash", "./docker-entrypoint.sh"]