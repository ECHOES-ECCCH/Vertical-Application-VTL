package pl.psnc.vtl.portal.services.transcriptions;

import pl.psnc.vtl.portal.services.I18nException;

public class NoUserWithGivenEmailException extends I18nException {

    private static final String MESSAGE_PREFIX = "transcriptions.error.";


    public NoUserWithGivenEmailException(String message, String messageCode) {
        super(message, MESSAGE_PREFIX + messageCode);
    }

}
