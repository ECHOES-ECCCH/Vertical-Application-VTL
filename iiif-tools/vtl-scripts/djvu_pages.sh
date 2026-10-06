#!/usr/bin/env bash

function printUsage() {
	echo Usage:
	echo djvu_pages.sh INPUT_DJVU_ABSOLUTE_PATH
}

function process() {
# get number of pages
nop=$(djvused -e n "$djvuFile")
echo $nop
exit 0
}

if [ $# -lt 1 ]
then
	printUsage
else
	djvuFile=$1
	process
fi
