package pl.psnc.vtl.portal.model.users;

import pl.psnc.vtl.portal.services.I18nException;

public class QuotaException extends I18nException {


    public QuotaException(String message, String messageCode) {
        super(message, messageCode);
    }
}
