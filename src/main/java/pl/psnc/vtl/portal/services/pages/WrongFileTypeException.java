package pl.psnc.vtl.portal.services.pages;

import lombok.Getter;
import pl.psnc.vtl.portal.services.I18nException;

@Getter
public class WrongFileTypeException extends I18nException {

    private static final String MESSAGE_PREFIX = "pages.error.";

    private String filename;

    public WrongFileTypeException(String message, String messageCode, String filename) {
        super(message, MESSAGE_PREFIX + messageCode);
        this.filename = filename;
    }

}
