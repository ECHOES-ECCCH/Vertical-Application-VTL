FROM maven:3.9-eclipse-temurin-11 as build

RUN apt-get update && apt-get install git

WORKDIR /usr/src/app
COPY pom.xml ./
RUN mvn verify --fail-never

COPY . .

RUN ./buildinfo.sh && mvn package

FROM eclipse-temurin:11-jre
WORKDIR /usr/app
ENV TZ=Europe/Warsaw

RUN apt-get update \
    && apt-get install -y tini \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

COPY ./vtl-config /var/vtl-config
COPY --from=build /usr/src/app/target/wlt-portal-*.jar ./wlt-portal.jar
COPY docker-entrypoint.sh .

RUN chgrp -R 0 /usr/app && chmod -R g=u /usr/app && chmod +x docker-entrypoint.sh

ENV JAVA_OPTS_MEM="-Xms512m -Xmx4g" \
    JAVA_OPTS_ADV="-server -Dfile.encoding=UTF-8 " \
    JAVA_OPTS_XX="-XX:+AlwaysPreTouch -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:ParallelGCThreads=20 -XX:ConcGCThreads=5 \
       -XX:+ExplicitGCInvokesConcurrent -XX:InitiatingHeapOccupancyPercent=70 -XX:+UseStringDeduplication -XX:MaxMetaspaceSize=1G \
       -XX:ReservedCodeCacheSize=384m -XX:+UseCodeCacheFlushing -XX:+HeapDumpOnOutOfMemoryError \
       -XX:HeapDumpPath=/var/dumps/jvm`date`.hprof" \
    JAVA_OPTS_JMX="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010 \
       -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false \
       -Dcom.sun.management.jmxremote.ssl=false" \
    JAVA_OPTS_DEBUG="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:9998" \
    JAVA_OPTS=""

ENTRYPOINT ["/usr/bin/tini", "--", "./docker-entrypoint.sh"]
