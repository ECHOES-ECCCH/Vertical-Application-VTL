#!/usr/bin/env bash

function printUsage() {
	echo Usage:
	echo djvudump.sh INPUT_FILE_ABSOLUTE_PATH
}

function process() {
djvudump "$inputFile"
}

if [ $# -lt 1 ]
then
	printUsage
else
	inputFile=$1
	process
fi
