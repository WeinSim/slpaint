#!/bin/bash
# Script for running the program. To be copied to /usr/local/bin/slpaint

JAR=$(ls ~/.m2/repository/com/weinsim/slpaint/*/slpaint-*.jar | tail -n 1)

java \
    --enable-native-access=ALL-UNNAMED \
    --sun-misc-unsafe-memory-access=allow \
    -jar "$JAR" \
    "$@"