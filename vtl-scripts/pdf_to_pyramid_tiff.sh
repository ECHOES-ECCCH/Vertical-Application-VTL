#!/usr/bin/env bash

function printUsage() {
	echo Usage:
	echo pdf_to_pyramid_tiff.sh INPUT_PDF_ABSOLUTE_PATH OUTPUT_FOLDER_PATH [OPTIONAL: PDF_DPI]
	echo Both folders have to exist
}

function process() {
echo pdf_to_pyramid_tiff

# get number of pages
nop=$(pdfinfo "$pdfFile" | grep -a Pages: | awk '{print $2}')
echo "tiff-pages:${nop}"

if [ $nop == 1 ]; then
  echo "one page file"
  i=0
  pageNo=$(printf -v i "%04d" $i ; echo $i)
  vips pdfload "$pdfFile" "$pdfFile$pageNo.tif" --page $i --dpi=$pdfDpi
  name=$(basename -- "$pdfFile")
  name="${name%.*}"
  vips tiffsave "$pdfFile$pageNo.tif" "$outFolder/${name}_Page$pageNo.tif" --compression=jpeg --Q=75 --tile --tile-width=128 --tile-height=128 --pyramid
  echo "tiff-file:${name}_Page$pageNo.tif"
  rm "$pdfFile$pageNo.tif"
else
  echo $nop" pages file"
  for (( i=0; i<$nop; i++ ))
    do
    tempNo=$(printf -v i "%04d" $i ; echo $i)
    vips pdfload "$pdfFile" "$pdfFile$tempNo.tif" --page $i --dpi=$pdfDpi
  done

  for (( i=1; i<$nop+1; i++ ))
    do
    j=$(($i-1))
    tempNo=$(printf -v j "%04d" $j ; echo $j)
    pageNo=$(printf -v i "%04d" $i ; echo $i)
    name=$(basename -- "$pdfFile")
    name="${name%.*}"
    vips tiffsave "$pdfFile$tempNo.tif" "$outFolder/${name}_Page$pageNo.tif" --compression=jpeg --Q=75 --tile --tile-width=128 --tile-height=128 --pyramid
    echo "tiff-file:${name}_Page$pageNo.tif"
  done

  for (( i=0; i<$nop; i++ ))
    do
    tempNo=$(printf -v i "%04d" $i ; echo $i)
    rm "$pdfFile$tempNo.tif"
  done
fi

echo "tiff-status:done"
exit 0
}

if [ $# -lt 2 ]
then
	printUsage
else
	pdfFile=$1
	outFolder=$2
	pdfDpi=${3:-200}
	process
fi
