FROM maven:3.9-eclipse-temurin-11 as build

WORKDIR /usr/src/app

COPY pom.xml ./
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package

FROM debian:bookworm-slim

ENV TZ=Europe/Warsaw

RUN DEBIAN_FRONTEND=noninteractive apt update \
  && apt install -y vim wget gnupg tini \
  && echo "deb https://notesalexp.org/tesseract-ocr5/bookworm/ bookworm main" >> /etc/apt/sources.list \
  && wget -qO- https://notesalexp.org/debian/alexp_key.asc | apt-key add - \
  && apt update \
  && apt install -qq -y tesseract-ocr-all ghostscript default-jre \
  && mkdir -p /usr/share/man/man1 \
  && apt clean && rm -rf /var/lib/apt/lists/*

#ENV VTL_USER="vtl" \
#    VTL_UID="5555" \
#    VTL_GROUP="vtl" \
#    VTL_GID="5555"

#RUN set -ex; \
#  groupadd -r --gid "$VTL_GID" "$VTL_GROUP"; \
#  useradd -r --uid "$VTL_UID" --gid "$VTL_GID" "$VTL_USER"

WORKDIR /usr/app
COPY --from=build /usr/src/app/target/wlt-ocr-tools-*.jar ./wlt-ocr-tools.jar
COPY docker-entrypoint.sh .

RUN chmod +x docker-entrypoint.sh

ENV LC_ALL=C \
    JAVA_OPTS_MEM="-Xms512m -Xmx4g" \
    JAVA_OPTS_ADV="-server -Dfile.encoding=UTF-8 " \
    JAVA_OPTS_XX="-XX:+AlwaysPreTouch -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:ParallelGCThreads=20 -XX:ConcGCThreads=5 \
       -XX:+ExplicitGCInvokesConcurrent -XX:InitiatingHeapOccupancyPercent=70 -XX:+UseStringDeduplication -XX:MaxMetaspaceSize=1G \
       -XX:ReservedCodeCacheSize=384m -XX:+UseCodeCacheFlushing -XX:+HeapDumpOnOutOfMemoryError \
       -XX:HeapDumpPath=/var/dumps/jvm`date`.hprof" \
    JAVA_OPTS_DEBUG="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:9999" \
    JAVA_OPTS=""

EXPOSE 5557
ENTRYPOINT ["/usr/bin/tini", "--", "./docker-entrypoint.sh"]
