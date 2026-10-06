package pl.psnc.vtl.portal.services.pages;

import lombok.Getter;


enum PageCRUDErrorMessages {
    ILLEGAL_CREATION_MODE("pages.delete.zip.illegal-creation-mode");

    @Getter
    private final String errorMessage;

    PageCRUDErrorMessages(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
