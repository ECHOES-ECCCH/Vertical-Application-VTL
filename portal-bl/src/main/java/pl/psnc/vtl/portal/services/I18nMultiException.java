package pl.psnc.vtl.portal.services;

import lombok.Getter;

import java.util.List;

@Getter
public class I18nMultiException extends Exception {

    private List<I18nException> items;

    public I18nMultiException(String message, List<I18nException> items) {
        super(message);
        this.items = items;
    }

    @Override
    public String toString() {
        String s = super.toString();
        return s + "; items: " + items.toString();
    }

}
