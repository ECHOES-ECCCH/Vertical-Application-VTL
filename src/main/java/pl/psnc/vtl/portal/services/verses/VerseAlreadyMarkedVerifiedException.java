package pl.psnc.vtl.portal.services.verses;

import pl.psnc.vtl.portal.services.I18nException;

public class VerseAlreadyMarkedVerifiedException extends I18nException {

    public VerseAlreadyMarkedVerifiedException(String message) {
        super(message, "verses.error.alreadyVerified");
    }

}
