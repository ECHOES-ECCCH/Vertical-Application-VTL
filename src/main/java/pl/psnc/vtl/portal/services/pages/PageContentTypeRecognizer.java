package pl.psnc.vtl.portal.services.pages;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;

import java.util.Arrays;

@UtilityClass
public class PageContentTypeRecognizer {

    public TranscriptionPageContentType recognize(String filename) {
        return recognize(filename, null);
    }

    public TranscriptionPageContentType recognize(String filename, String contentType) {
        return Arrays.stream(TranscriptionPageContentType.values())
                .filter(pct -> (StringUtils.isNotBlank(contentType) && pct.getContentType().contains(contentType)) || pct.getExtensions().contains(FilenameUtils.getExtension(filename).toLowerCase()))
                .findFirst().orElseThrow(() -> new PageContentImportException("Unrecognized file type: " + filename + ", " + contentType, PageContentImportErrorMessages.WRONG_FILE.getErrorMessage()));
    }

}
