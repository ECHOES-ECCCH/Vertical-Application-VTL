FROM maven:3.9-eclipse-temurin-11 as build

WORKDIR /usr/src/app

COPY pom.xml ./
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package


FROM ubuntu:22.04 as unpacker
COPY ./vlt-resources/calibre*.txz /tmp/
RUN apt update && apt install -y xz-utils && mkdir /tmp/calibre && tar xvf /tmp/calibre*.txz -C /tmp/calibre && rm /tmp/calibre*.txz


FROM ubuntu:22.04

ENV TZ=Europe/Warsaw

USER root

#ENV VTL_USER="vtl" \
#    VTL_UID="5555" \
#    VTL_GROUP="vtl" \
#    VTL_GID="5555"

RUN \
  set -eux; \
  \
  # Using a recent, stable version of ImageMagick 7
  IM_VERSION=7.1.1-30; \
  \
  # Define all build-time packages together
  BUILD_DEPS=" \
    build-essential ruby-dev libmagickwand-dev \
    libtiff-dev zlib1g-dev libleptonica-dev \
    libxml2-dev libxslt-dev wget pkg-config \
    libjpeg-dev libpng-dev \
  "; \
  \
  # 1. Update apt and install ALL dependencies (runtime and build-time)
  DEBIAN_FRONTEND=noninteractive; \
  apt-get update; \
  apt-get install -y --no-install-recommends \
    openjdk-11-jre curl \
    ghostscript libvips42 libgl1 libvips-tools poppler-utils \
    exactimage libtiff5 zlib1g djvulibre-bin libdjvulibre21 \
    libxcomposite1 libxdamage1 libxcursor1 libxrandr2 libxi6 xdg-utils \
    ruby rubygems-integration libxml2 libxslt1.1 tini \
    $BUILD_DEPS; \
  \
  # 2. Compile and install ImageMagick 7 from source
  mkdir -p /usr/local/src/imagemagick; \
  curl -L https://github.com/ImageMagick/ImageMagick/archive/refs/tags/${IM_VERSION}.tar.gz | \
  tar xz -C /usr/local/src/imagemagick --strip-components=1; \
  cd /usr/local/src/imagemagick; \
  ./configure; \
  make -j$(nproc); \
  make install; \
  ldconfig; \
  cd /; \
  rm -rf /usr/local/src/imagemagick; \
  \
  # 3. Compile and install jbig2
  mkdir jbig2; \
  curl -L https://github.com/agl/jbig2enc/archive/0.29.tar.gz | \
  tar xz -C jbig2 --strip-components=1; \
  cd jbig2; \
  ./autogen.sh && ./configure && make && make install; \
  cd ..; \
  rm -rf jbig2; \
  ldconfig; \
  \
  # 4. Compile and install gems
  # Force Nokogiri to use system libs to remove the warning
  export NOKOGIRI_USE_SYSTEM_LIBRARIES=true; \
  # Install the latest RMagick, which will find our compiled ImageMagick 7
  gem install rmagick nokogiri iconv hpricot pdfbeads; \
  \
  # 5. Remove ALL build-time dependencies.
  apt-get purge -y --auto-remove $BUILD_DEPS; \
  \
  # 6. Clean up apt cache.
  apt-get clean; \
  rm -rf /var/lib/apt/lists/*

COPY --from=unpacker /tmp/calibre /opt/calibre
RUN mkdir -p /usr/share/applications /usr/share/desktop-directories && /opt/calibre/calibre_postinstall

COPY src/main/resources/imagemagick/policy.xml  /etc/ImageMagick-6/policy.xml

WORKDIR /usr/app

COPY ./vtl-scripts /var/vtl-scripts
COPY docker-entrypoint.sh .
RUN chmod +x /var/vtl-scripts/* docker-entrypoint.sh 

#RUN set -ex; \
#  groupadd -r --gid "$VTL_GID" "$VTL_GROUP"; \
#  useradd -r --uid "$VTL_UID" --gid "$VTL_GID" "$VTL_USER"

COPY --from=build /usr/src/app/target/wlt-iiif-tools-*.jar ./wlt-iiif-tools.jar

ENV JAVA_OPTS_MEM="-Xms512m -Xmx4g" \
    JAVA_OPTS_ADV="-server -Dfile.encoding=UTF-8 " \
    JAVA_OPTS_XX="-XX:+AlwaysPreTouch -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:ParallelGCThreads=20 -XX:ConcGCThreads=5 \
       -XX:+ExplicitGCInvokesConcurrent -XX:InitiatingHeapOccupancyPercent=70 -XX:+UseStringDeduplication -XX:MaxMetaspaceSize=1G \
       -XX:ReservedCodeCacheSize=384m -XX:+UseCodeCacheFlushing -XX:+HeapDumpOnOutOfMemoryError \
       -XX:HeapDumpPath=/var/dumps/jvm`date`.hprof" \
    JAVA_OPTS_DEBUG="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:9995" \
    JAVA_OPTS=""

EXPOSE 5556
ENTRYPOINT ["/usr/bin/tini", "--", "./docker-entrypoint.sh"]
