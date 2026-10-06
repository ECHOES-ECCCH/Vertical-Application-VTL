package pl.psnc.vtl.portal.services.domains;

import pl.psnc.vtl.portal.services.I18nException;

public class DomainNotAvailableForExternalEditException extends I18nException {

    public DomainNotAvailableForExternalEditException(String message) {
        super(message, "domain.error.not.available.external");
    }
}
