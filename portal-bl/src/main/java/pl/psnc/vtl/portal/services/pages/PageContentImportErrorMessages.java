package pl.psnc.vtl.portal.services.pages;

import lombok.Getter;


public enum PageContentImportErrorMessages {
    UNSUPPORTED_FILE_TYPE("unsupportedFileType"),
    UNRECOGNIZABLE_PAGE("unrecognizablePage"),
    WRONG_FILE("wrongFileProvided");

    @Getter
    private final String errorMessage;

    PageContentImportErrorMessages(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
