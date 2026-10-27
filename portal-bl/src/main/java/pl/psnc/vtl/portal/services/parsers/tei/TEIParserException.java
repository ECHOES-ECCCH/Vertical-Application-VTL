package pl.psnc.vtl.portal.services.parsers.tei;

import pl.psnc.vtl.portal.services.I18nException;

public class TEIParserException extends I18nException {

    public TEIParserException(String message, String messageCode) {
        super(message, messageCode);
    }
}
