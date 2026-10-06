package pl.psnc.vtl.portal.services.verses;

import pl.psnc.vtl.portal.services.I18nException;

public class IncorrectPositionException extends I18nException {

    private static final String MESSAGE_PREFIX = "verses.error.";


    public IncorrectPositionException(String message, String messageCode) {
        super(message, MESSAGE_PREFIX + messageCode);
    }

}
