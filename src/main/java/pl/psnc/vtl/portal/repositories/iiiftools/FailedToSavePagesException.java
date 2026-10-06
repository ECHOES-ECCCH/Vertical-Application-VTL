package pl.psnc.vtl.portal.repositories.iiiftools;

import pl.psnc.vtl.portal.services.I18nException;

public class FailedToSavePagesException extends I18nException {

    public FailedToSavePagesException(String message) {
        super(message, "pages.error.cannotSave");
    }
}

