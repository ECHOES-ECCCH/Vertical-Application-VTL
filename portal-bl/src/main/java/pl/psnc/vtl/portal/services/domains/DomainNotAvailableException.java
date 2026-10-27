package pl.psnc.vtl.portal.services.domains;

import pl.psnc.vtl.portal.services.I18nException;

public class DomainNotAvailableException extends I18nException {

    public DomainNotAvailableException(String message, String fileType) {
        super(message, "domain.error.not.available." + fileType);
    }
}
