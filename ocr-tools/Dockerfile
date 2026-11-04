# syntax=docker/dockerfile:1

############################
# 1. Build the Java app
############################
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /usr/src/app

COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package \
 && cp target/wlt-ocr-tools-*.jar /wlt-ocr-tools.jar

############################
# 2. Build the Kraken venv (pip and caches stay out of the final image)
############################
FROM debian:bookworm-slim AS kraken-build
ARG DEBIAN_FRONTEND=noninteractive
ARG KRAKEN_VERSION=
RUN apt-get update \
 && apt-get install -y --no-install-recommends python3 python3-venv \
 && rm -rf /var/lib/apt/lists/*
RUN --mount=type=cache,target=/root/.cache/pip \
    python3 -m venv /opt/kraken \
 && /opt/kraken/bin/pip install --upgrade pip \
 && /opt/kraken/bin/pip install \
      --extra-index-url https://download.pytorch.org/whl/cpu \
      "kraken${KRAKEN_VERSION:+==${KRAKEN_VERSION}}"

############################
# 3. Runtime image
############################
FROM debian:bookworm-slim

ARG DEBIAN_FRONTEND=noninteractive
# Install only the Tesseract languages you actually use
ARG TESS_LANGS="osd eng pol"

ENV TZ=Europe/Warsaw \
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8

# Repo key without apt-key (deprecated); apt reads .asc keys directly
ADD --chmod=644 https://notesalexp.org/debian/alexp_key.asc /etc/apt/keyrings/alexp.asc

RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates \
 && echo "deb [signed-by=/etc/apt/keyrings/alexp.asc] https://notesalexp.org/tesseract-ocr5/bookworm/ bookworm main" \
      > /etc/apt/sources.list.d/tesseract.list \
 && apt-get update \
 && apt-get install -y --no-install-recommends \
      tini \
      tesseract-ocr $(for l in $TESS_LANGS; do printf 'tesseract-ocr-%s ' "$l"; done) \
      ghostscript imagemagick \
      python3 libgl1 libglib2.0-0 \
 && sed -i -E 's#<policy domain="coder" rights="none" pattern="(PS|PS2|PS3|EPS|PDF|XPS)" />#<policy domain="coder" rights="read|write" pattern="\1" />#' \
      /etc/ImageMagick-6/policy.xml \
 && rm -rf /var/lib/apt/lists/*

# Java 11 JRE matching the build JDK (headless, no X11 deps, no man1 hack)
COPY --from=eclipse-temurin:11-jre /opt/java/openjdk /opt/java/openjdk
ENV JAVA_HOME=/opt/java/openjdk \
    PATH=/opt/java/openjdk/bin:$PATH

# Kraken, available to all users
COPY --from=kraken-build /opt/kraken /opt/kraken

# Shared, writable XDG dirs so kraken works for any UID, even without a passwd entry
ENV XDG_CACHE_HOME=/var/cache/xdg \
    XDG_CONFIG_HOME=/etc/xdg-user \
    XDG_DATA_HOME=/usr/local/share/xdg \
    MPLCONFIGDIR=/var/cache/xdg/matplotlib \
    TORCH_HOME=/var/cache/xdg/torch

RUN ln -s /opt/kraken/bin/kraken /usr/local/bin/kraken \
 && ln -s /opt/kraken/bin/ketos  /usr/local/bin/ketos \
 && for d in "$XDG_CACHE_HOME" "$XDG_CONFIG_HOME" "$XDG_DATA_HOME"; do \
      mkdir -p "$d/htrmopo" && chmod 1777 "$d" && chmod 0777 "$d/htrmopo"; \
    done \
 && mkdir -p "$MPLCONFIGDIR" "$TORCH_HOME" /var/dumps \
 && chmod 0777 "$MPLCONFIGDIR" "$TORCH_HOME" \
 && chmod 1777 /var/dumps \
 && useradd --system --uid 10001 --create-home app \
 && kraken --version && convert -version && tesseract --version && java -version

WORKDIR /usr/app
COPY --from=build /wlt-ocr-tools.jar ./wlt-ocr-tools.jar
COPY --chmod=755 docker-entrypoint.sh .

ENV JAVA_OPTS_MEM="-XX:InitialRAMPercentage=10 -XX:MaxRAMPercentage=75" \
    JAVA_OPTS_ADV="-Dfile.encoding=UTF-8" \
    JAVA_OPTS_XX="-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+ExplicitGCInvokesConcurrent \
       -XX:InitiatingHeapOccupancyPercent=70 -XX:+UseStringDeduplication -XX:MaxMetaspaceSize=1G \
       -XX:ReservedCodeCacheSize=384m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/dumps" \
    JAVA_OPTS_DEBUG="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:9999" \
    JAVA_OPTS=""

USER app
EXPOSE 5557
ENTRYPOINT ["/usr/bin/tini", "--", "./docker-entrypoint.sh"]