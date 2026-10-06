#!/usr/bin/env bash

function printUsage() {
	echo Usage:
	echo image_pages.sh INPUT_IMAGE_ABSOLUTE_PATH
}

function process() {
# get number of pages
filename=$(basename -- "$inputFile")
extension="${filename##*.}"
extension="${extension,,}"
filename="${filename%.*}"

nop=-1
if [ "$extension" = "jpg" ] || [ "$extension" = "jpeg" ] || [ "$extension" = "png" ]
then
  nop=1
fi

if [ "$extension" = "djv" ] || [ "$extension" = "djvu" ]
then
  nop=$(djvused -e n $inputFile)
fi

if [ "$extension" = "tif" ] || [ "$extension" = "tiff" ]
then
  nop=$(tiffinfo $inputFile 2> /dev/null | grep -c "TIFF Directory")
fi

if [[ $nop -eq 0 ]]
then
  nop=1
fi

echo $nop
exit 0
}

if [ $# -lt 1 ]
then
	printUsage
else
	inputFile=$1
	process
fi
