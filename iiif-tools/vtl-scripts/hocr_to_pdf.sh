#!/usr/bin/env bash

function printUsage() {
	echo Usage:
	echo script.sh INPUT_IMAGE_ABSOLUTE_PATH
}

function process() {
pdfBackgroundDpi=100
pdfForegroundDpi=100

mkdir -p $tmpDirPath
cd $tmpDirPath

pageIdx=0
for imageFile in $(ls $tmpDirPath  -I '*.html'); do
    [ -f "$imageFile" ] || break
    pageNo=$(printf -v pageIdxStr "%04d" $pageIdx ; echo $pageIdxStr)
    corename=$(basename -- "$imageFile")
    name="${corename%.*}"
    convert "$imageFile" -density 96 -units PixelsPerInch "${name}tif.tif"
    rm "$imageFile"
    mv "${name}tif.tif" "${name}.tif"
    let "pageIdx += 1"
done

outp=$(pdfbeads -b JPEG -B 96 -o $generatedPdfName && chmod 777 $generatedPdfName)
echo $outp

mv $tmpDirPath/$generatedPdfName $ebookDirPath
rm -r $tmpDirPath

exit 0
}

if [ $# -lt 2 ]
then
	printUsage
else
	tmpDirPath=$1
	ebookDirPath=$2
	generatedPdfName=$3
	process
fi