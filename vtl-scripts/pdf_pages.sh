#!/usr/bin/env bash

function printUsage() {
	echo Usage:
	echo pdf_pages.sh INPUT_PDF_ABSOLUTE_PATH
}

function process() {
# get number of pages
nop=$(pdfinfo "$pdfFile" 2>&1 | grep -a Pages: | awk '{print $2}')
echo $nop
exit 0
}

if [ $# -lt 1 ]
then
	printUsage
else
	pdfFile=$1
	process
fi
