package pl.psnc.vtl.portal.services.users;

import pl.psnc.vtl.portal.services.I18nException;

public class UserGroupAlreadyExsitsException extends I18nException {

    private static final String MESSAGE_PREFIX = "groups.error.";


    public UserGroupAlreadyExsitsException(String message, String messageCode) {
        super(message, MESSAGE_PREFIX + messageCode);
    }

}
