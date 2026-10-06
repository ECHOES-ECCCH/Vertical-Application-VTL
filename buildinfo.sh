#!/usr/bin/env bash

echo "commitId="$(git rev-parse --short HEAD)  > ./src/main/resources/.buildinfo
echo "buildDate="$(date +%s)  >> ./src/main/resources/.buildinfo
