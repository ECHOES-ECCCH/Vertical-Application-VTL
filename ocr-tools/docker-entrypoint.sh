#!/bin/sh

echo java $JAVA_OPTS_MEM $JAVA_OPTS_ADV $JAVA_OPTS_XX $JAVA_OPTS_DEBUG $JAVA_OPTS \
    -Dspring.profiles.active=$SPRING_ACTIVE_PROFILE -jar wlt-ocr-tools.jar $APP_ARGUMENTS
exec java $JAVA_OPTS_MEM $JAVA_OPTS_ADV $JAVA_OPTS_XX $JAVA_OPTS_DEBUG $JAVA_OPTS \
    -Dspring.profiles.active=$SPRING_ACTIVE_PROFILE -jar wlt-ocr-tools.jar $APP_ARGUMENTS
