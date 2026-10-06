#!/usr/bin/env bash

function printUsage() {
	echo Usage:
	echo script.sh INPUT_IMAGE_ABSOLUTE_PATH
}

function process() {
outp=$(cd $tmpDirPath && ebook-convert $epubFileName $mobiFileName)
echo $outp

createDirectoryIfDoNotExists=$(cd $tmpDirPath && mkdir -p ../$mobiFolder)
echo $createDirectoryIfDoNotExists

moveFile=$(cd $tmpDirPath && mv $mobiFileName ../$mobiFolder/$mobiFileName)
echo $moveFile

removeTmpFile=$(cd $tmpDirPath && rm $epubFileName)
echo $removeTmpFile

exit 0
}

if [ $# -lt 4 ]
then
	printUsage
else
	tmpDirPath=$1
	epubFileName=$2
	mobiFileName=$3
	mobiFolder=$4
	process
fi