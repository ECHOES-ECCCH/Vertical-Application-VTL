package pl.psnc.vtl.portal.services.registration;

import pl.psnc.vtl.portal.services.I18nException;

public class UserAlreadyExistsException extends I18nException {

    private static final String MESSAGE_PREFIX = "auth.error.";

    public UserAlreadyExistsException(String message, String messageCode) {
        super(message, MESSAGE_PREFIX + messageCode);
    }

}

