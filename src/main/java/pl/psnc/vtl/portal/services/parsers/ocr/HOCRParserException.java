package pl.psnc.vtl.portal.services.parsers.ocr;


import lombok.extern.slf4j.Slf4j;
import pl.psnc.vtl.portal.services.I18nException;

@Slf4j
public class HOCRParserException extends I18nException {

    public HOCRParserException(String message, String messageCode) {
        super(message, messageCode);
    }
}
