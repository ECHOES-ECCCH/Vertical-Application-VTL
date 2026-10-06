package pl.psnc.vtl.iiif.converters;

import lombok.Data;

import java.util.ArrayList;
import java.util.Scanner;

@Data
public class ConversionResult {

    private ArrayList<String> filesNames = new ArrayList<>();

    private Integer numberOfPages = -1;

    private Boolean validConversion = true;

    private ConversionResult(){
    }

    public  ConversionResult(String results){
        ConversionResultParser conversionResultParser = new ConversionResultParser();
        conversionResultParser.parseScriptResults(results);
    }

    public boolean validate()
            throws ConversionException {
        if (filesNames.size() != numberOfPages)
            throw  new ConversionException("Conversion process return " + filesNames.size() + " but input file has " + numberOfPages + "pages.");
        if (filesNames.size() == 0){
            throw  new ConversionException("Conversion process return zero pages files!");
        }
        if(!validConversion) {
            throw new ConversionException("Conversion process failed due to invalid file format.");
        }
        return  true;
    }

    class ConversionResultParser {
        private  static  final String  TIFF_FILE_MARKER = "tiff-file";
        private  static  final String  TIFF_PAGES_MARKER = "tiff-pages";
        private static final String CONVERSION_EXCEPTION_MARKER = "convert-im6.q16";
        private static final String WARNING_MARKER = "warning";

        private void parseLine(String line) {
            if (line == null || line.isEmpty())
                return;

            try {
                if (line.startsWith(TIFF_FILE_MARKER)) {
                    filesNames.add(line.split(":")[1]);
                }
                if (line.startsWith(TIFF_PAGES_MARKER)) {
                    numberOfPages = Integer.parseInt(line.split(":")[1]);
                }
                if (line.startsWith(CONVERSION_EXCEPTION_MARKER) && !line.contains(WARNING_MARKER)) {
                    validConversion = false;
                }
            }
            catch (Exception ignore){}
        }

        public void parseScriptResults(String results) {
            try (Scanner scanner = new Scanner(results)) {
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    parseLine(line);
                }
            }
            catch (Exception ignore){}
        }
    }
}