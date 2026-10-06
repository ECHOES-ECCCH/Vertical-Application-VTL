package pl.psnc.vtl.portal.services.pages;

import lombok.Getter;
import pl.psnc.vtl.portal.services.I18nException;

@Getter
public class PageContentImportException extends I18nException {

    private static final String MESSAGE_PREFIX = "import.error.";

    private String filename;

    public PageContentImportException(String message, String messageCode) {
        super(message, MESSAGE_PREFIX + messageCode);
    }

    public PageContentImportException(String message, String messageCode, String filename) {
        super(message, MESSAGE_PREFIX + messageCode);
        this.filename = filename;
    }

}
