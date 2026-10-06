package pl.psnc.vtl.portal.services.imports;

import lombok.Getter;


public enum URLErrorMessages {
    MALFORMED("imports.error.url.malformed"),
    SECURED("imports.error.url.secured"),
    UNRESPONSIVE("imports.error.url.unresponsive"),
    NOT_PROCESSABLE("imports.error.url.not-processable"),
    NOT_FOUND("imports.error.url.not-found");


    @Getter
    private final String errorMessage;
    URLErrorMessages(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
