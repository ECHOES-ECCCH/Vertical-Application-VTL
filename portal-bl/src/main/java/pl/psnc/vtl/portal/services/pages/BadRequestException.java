package pl.psnc.vtl.portal.services.pages;

import pl.psnc.vtl.portal.services.I18nException;

public class BadRequestException extends I18nException {

    private static final String MESSAGE_PREFIX = "pages.error.";


    public BadRequestException(String message, String messageCode) {
        super(message, MESSAGE_PREFIX + messageCode);
    }

}
