FROM debian:trixie-slim AS iipbuild

ARG IIPSRV_VERSION=1.3
ARG IIPSRV_URL=https://github.com/ruven/iipsrv/archive/refs/tags/iipsrv-${IIPSRV_VERSION}.tar.gz

RUN set -eux; \
    apt-get update; \
    apt-get install -y --no-install-recommends \
        autoconf \
        automake \
        autotools-dev \
        ca-certificates \
        cmake \
        curl \
        g++ \
        libfcgi-dev \
        libjpeg-dev \
        libmemcached-dev \
        libopenjp2-7-dev \
        libtiff-dev \
        libtool \
        make \
        ncat \
        pkg-config \
        zlib1g-dev \
        libwebp-dev \
        libzstd-dev; \
    rm -rf /var/lib/apt/lists/*

WORKDIR /usr/src/iipsrv

# iipsrv 1.3; --enable-openjpeg uses Debian trixie's OpenJPEG 2.x pkg-config files.
# GCC 14 can expose missing transitive integer-type includes, so include cstdint globally.
RUN set -eux; \
    curl --fail --silent --show-error --location "$IIPSRV_URL" -o /tmp/iipsrv.tar.gz; \
    tar --extract --gzip --file /tmp/iipsrv.tar.gz --strip-components=1; \
    rm /tmp/iipsrv.tar.gz; \
    ./autogen.sh; \
    ./configure --enable-openjpeg CXXFLAGS="-include cstdint"; \
    make -j"$(nproc)"

FROM debian:trixie-slim AS publish

ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=Europe/Warsaw

RUN set -eux; \
    apt-get update; \
    apt-get install -y --no-install-recommends \
        apache2 \
        libapache2-mod-fcgid \
        libfcgi-bin \
        libjpeg-turbo-progs \
        libmemcached11 \
        libopenjp2-7 \
        libtiff6 \
        libgomp1 \
        libvips-tools \
        dmidecode \
        ipmitool \
        nfs-common \
        procps \
        sysstat \
        samba; \
    a2enmod rewrite headers; \
    a2enmod iipsrv || true; \
    apt-get clean; \
    rm -rf /var/lib/apt/lists/*

COPY iipsrv.conf /etc/apache2/mods-available/iipsrv.conf

RUN set -eux; \
    touch /etc/apache2/mods-available/iipsrv.load; \
    a2enmod iipsrv; \
    mkdir -p /usr/lib/iipimage-server

COPY --from=iipbuild /usr/src/iipsrv/src/iipsrv.fcgi /usr/lib/iipimage-server/

RUN set -eux; \
    sed -i 's+\tDocumentRoot /var/www/html+\tDocumentRoot /var/www+g' /etc/apache2/sites-available/000-default.conf; \
    sed -i '57i ServerName "iiif-server"' /etc/apache2/apache2.conf; \
    sed -i 's/<VirtualHost \\*:80>/<VirtualHost *:8080>/g' /etc/apache2/sites-enabled/000-default.conf; \
    sed -i 's/Listen 80/Listen 8080/g' /etc/apache2/ports.conf; \
    sed -i 's/APACHE_RUN_GROUP=www-data/APACHE_RUN_GROUP=root/g' /etc/apache2/envvars

COPY directory.conf /etc/apache2/sites-enabled/
COPY docker-entrypoint.sh ./

RUN chmod +x ./docker-entrypoint.sh && \
    chgrp -R 0 / 2> /dev/null || true && \
    chmod -R g=u / 2> /dev/null || true

EXPOSE 8080
ENTRYPOINT ["sh", "./docker-entrypoint.sh"]
