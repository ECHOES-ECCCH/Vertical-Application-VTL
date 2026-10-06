#!/usr/bin/env bash

function printUsage() {
	echo Usage:
	echo convert_to_tiff.sh INPUT_FILE_ABSOLUTE_PATH_WITHOUT_EXTENSION INPUT_FILE_EXTENSION
}

function process() {
echo convert_to_tiff

stdbuf -i0 -o0 -e0 convert -monitor "${inputFile}.${extension}" "${inputFile}.tif"

echo Done
exit 0
}

if [ $# -lt 2 ]
then
	printUsage
else
	inputFile=$1
	extension=$2
	process
fi