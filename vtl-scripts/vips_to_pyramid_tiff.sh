#!/usr/bin/env bash

function printUsage() {
	echo Usage:
	echo vips_to_pyramid_tiff.sh INPUT_FILE_ABSOLUTE_PATH OUTPUT_FOLDER_PATH [OPTIONAL: CONVERT_MODE]
	echo Both folders have to exist
	echo "CONVERT_MODE indicates the tool to be used to convert inputFile file into tiff. Possible values: ddjvu or convert"
	echo "by default, when the option is not specified, CONVERT_MODE mode is set to convert - the 'convert' tool from image magick is then used"
	echo "if ddjvu is set as CONVERT_MODE then 'ddjvu' from djvulibre is used to convert the file"
	echo "'ddjvu' is able to handle indirect djvu files, while 'convert' does not support such djvu files, but supports bundled djvu and other formats"
}

function process() {
echo vips_to_pyramid_tiff

corename=$(basename -- "$inputFile")
name="${corename%.*}"
extension="${corename##*.}"


# get number of pages
if [ "$extension" == "tif" ] || [ "$extension" == "tiff" ]; then
nop=$(tiffinfo $inputFile 2> /dev/null | grep -c "TIFF Directory")
  if [[ $nop == 0 ]]; then
    nop=$(identify $inputFile | wc -l)
  fi
elif [ "$extension" == "djv" ] || [ "$extension" == "djvu" ]; then
  nop=$(djvused -e n $inputFile)
else
  nop=1
fi
echo "tiff-pages:${nop}"

if [ "$conversionMode" == "ddjvu" ]; then
  echo using ddjvu to convert the file
  ddjvu -eachpage -format=tiff "$inputFile" "${inputFile}%04d.tif"
else
  echo using convert to convert the file
  stdbuf -i0 -o0 -e0 convert -monitor "$inputFile" "${inputFile}%04d.tif"
fi


if [ $nop == 1 ]; then
  echo "one page file"
  i=0
  if [ "$conversionMode" == "ddjvu" ]; then
    i=1
  fi
  pageNo=$(printf -v i "%04d" $i ; echo $i)
  vips tiffsave "$inputFile$pageNo.tif" "$outFolder/${name}_Page$pageNo.tif" --compression=jpeg --Q=75 --tile --tile-width=128 --tile-height=128 --pyramid
  echo "tiff-file:${name}_Page$pageNo.tif"
  rm "$inputFile$pageNo.tif"
else
  echo $nop" pages file"
  for (( i=1; i<$nop+1; i++ ))
    do
    j=$(($i-1))
    if [ "$conversionMode" == "ddjvu" ]; then
      j=$i
    fi
    tempNo=$(printf -v j "%04d" $j ; echo $j)
    pageNo=$(printf -v i "%04d" $i ; echo $i)
    vips tiffsave "$inputFile$tempNo.tif" "$outFolder/${name}_Page$pageNo.tif" --compression=jpeg --Q=75 --tile --tile-width=128 --tile-height=128 --pyramid
    echo "tiff-file:${name}_Page$pageNo.tif"
    rm "$inputFile$tempNo.tif"
  done
fi

echo Done
exit 0
}

if [ $# -lt 2 ]
then
	printUsage
else
	inputFile=$1
	outFolder=$2
	conversionMode=${3:-convert}
	process
fi
