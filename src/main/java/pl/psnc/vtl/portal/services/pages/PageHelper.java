package pl.psnc.vtl.portal.services.pages;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.services.ocr.OcrErrorMessages;
import pl.psnc.vtl.portal.services.transcriptions.ParsePagesRangeException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@UtilityClass
@Slf4j
public class PageHelper {
    private static final Pattern PAGES_PATTERN = Pattern.compile("^(\\d+)-(\\d+)$|^(\\d+)$");

    public String validateFileType(String filename) {
        String ext = FilenameUtils.getExtension(filename);
        if (!(ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("tif") ||
                ext.equalsIgnoreCase("tiff") || ext.equalsIgnoreCase("pdf") || ext.equalsIgnoreCase("png") ||
                ext.equalsIgnoreCase("djv") || ext.equalsIgnoreCase("djvu"))) {
            return null;
        }
        return StringUtils.stripAccents(URLDecoder.decode(filename, StandardCharsets.UTF_8).replace(" ", "_"));
    }


    public String preparePageName(Page page, TranscriptionPageContentType transcriptionPageContentType, Layer layer) {
        String baseName = FilenameUtils.removeExtension(page.getPath());
        String extension = "." + transcriptionPageContentType.getExtensions().get(0);
        if (layer != null) {
            if (page.getRelativeNo() != null) {
                String pageNumber = String.format("%04d", page.getRelativeNo());
                return baseName
                        + "_Page" + pageNumber
                        + "_" + layer.getName()
                        + extension;
            } else {
                return baseName
                        + "_" + layer.getName()
                        + extension;
            }
        } else if (page.getRelativeNo() != null) {
            String pageNumber = String.format("%04d", page.getRelativeNo());
            return baseName
                    + "_Page" + pageNumber
                    + extension;
        } else {
            return baseName +
                    extension;
        }
    }


    /**
     * Parse string containing comma separated list of page numbers or ranges and returns all page numbers by expanding
     * all ranges. Examples of possible values: '1', '1,2,3', '1,5,7-9', '1-5,7-10'. When there are any problems with parsing ParsePagesRangeException will be thrown.
     *
     * @param pages comma separated list of page numbers and ranges
     * @return all page numbers
     */
    public List<Integer> parsePages(String pages, int maxPageNumber) {
        if (StringUtils.isEmpty(pages)) {
            return IntStream.range(1, maxPageNumber + 1).boxed().collect(Collectors.toList());
        }

        List<Integer> pageRelativeNumbers = new ArrayList<>();
        String[] ranges = pages.split(",");
        for (String range : ranges) {
            pageRelativeNumbers.addAll(extractRangePages(range, maxPageNumber));
        }
        return pageRelativeNumbers;
    }

    private List<Integer> extractRangePages(String range, int maxPageNumber) {
        try {
            Matcher matcher = PAGES_PATTERN.matcher(range);
            if (matcher.find()) {
                if (matcher.group(3) == null) {
                    return generatePages(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
                            maxPageNumber);
                } else {
                    return generateSinglePage(Integer.parseInt(matcher.group(3)), maxPageNumber);
                }
            }
        } catch (Exception e) {
            throw new ParsePagesRangeException(range, OcrErrorMessages.PAGE_RANGE_INVALID.getErrorMessage());
        }
        throw new ParsePagesRangeException(range, OcrErrorMessages.PAGE_RANGE_INVALID.getErrorMessage());
    }

    private List<Integer> generateSinglePage(int pageNumber, int maxPageNumber) {
        if (pageNumber <= maxPageNumber) {
            return Collections.singletonList(pageNumber);
        }
        throw new IllegalArgumentException();
    }

    private List<Integer> generatePages(Integer start, Integer end, int maxPageNumber) {
        if (start > maxPageNumber || end > maxPageNumber) {
            throw new IllegalArgumentException();
        }
        return start > end ?
                IntStream.rangeClosed(end, start).boxed().collect(Collectors.toList()) :
                IntStream.rangeClosed(start, end).boxed().collect(Collectors.toList());
    }
}
