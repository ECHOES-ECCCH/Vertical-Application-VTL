package pl.psnc.vtl.portal.services;

import lombok.Getter;

@Getter
public abstract class I18nException extends RuntimeException {

    private String messageCode;


    public I18nException(String message, String messageCode) {
        super(message);
        this.messageCode = messageCode;
    }


    public I18nException(String message, Throwable cause, String messageCode) {
        super(message, cause);
        this.messageCode = messageCode;
    }


    @Override
    public String toString() {
        String s = super.toString();
        return s + "; messageCode: " + messageCode;
    }

}
