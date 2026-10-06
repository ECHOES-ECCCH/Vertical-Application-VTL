package pl.psnc.vtl.portal.services.imports;

import lombok.Getter;


public enum TextFormatErrorMessages {
    MALFORMED("imports.text.format.malformed"),
    WRONG_TYPE("imports.text.format.wrong-type"),
    UNSUPPORTED_TYPES("imports.text.format.unsupported-types"),
    LINK_LESS("imports.text.format.link-less");
    @Getter
    private final String errorMessage;

    TextFormatErrorMessages(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
