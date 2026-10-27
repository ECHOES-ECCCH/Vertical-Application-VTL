package pl.psnc.vtl.portal.controllers.transcriptions;

import pl.psnc.vtl.portal.services.I18nException;

public class PageTooLargeException extends I18nException {

    public PageTooLargeException(int max) {
        super("Page size must not be greater than "+ max + "!", "pages.error.pageToLarge");
    }
}
