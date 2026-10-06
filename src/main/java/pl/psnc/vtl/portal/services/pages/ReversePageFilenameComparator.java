package pl.psnc.vtl.portal.services.pages;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReversePageFilenameComparator implements Comparator<String> {

    @Override
    public int compare(String filename1, String filename2) {
        int page1Number = getNumberFromFilename(filename1);
        int page2Number = getNumberFromFilename(filename2);

        if (page1Number != page2Number) {
            return page2Number - page1Number;
        } else {
            String page1Suffix = getSuffixFromFilename(filename1);
            String page2Suffix = getSuffixFromFilename(filename2);

            if (page1Suffix == null) {
                return 1;
            }
            if (page2Suffix == null) {
                return -1;
            }

            return page2Suffix.compareTo(page1Suffix);
        }
    }


    public int getNumberFromFilename(String filename) {
        String baseName = FilenameUtils.getBaseName(filename);
        Matcher m = Pattern.compile("(.*[^0-9])?([0-9]+)").matcher(baseName);
        if (!m.find())
            return 0;
        return NumberUtils.toInt(m.group(2));
    }


    public String getSuffixFromFilename(String filename) {
        String baseName = FilenameUtils.getBaseName(filename);

        int number = getNumberFromFilename(filename);
        String stringNumber = String.valueOf(number);
        int idx = filename.indexOf(stringNumber);
        String suffix = baseName.substring(idx + stringNumber.length());

        if (suffix.equals("")) {
            return null;
        }
        return suffix;
    }

}
