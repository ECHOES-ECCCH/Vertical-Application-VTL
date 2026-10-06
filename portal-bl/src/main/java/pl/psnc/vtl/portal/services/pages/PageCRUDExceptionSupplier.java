package pl.psnc.vtl.portal.services.pages;

import pl.psnc.vtl.portal.controllers.ErrorResponseMessage;
import pl.psnc.vtl.portal.controllers.RequestFieldsValidationException;

class PageCRUDExceptionSupplier {

    static RequestFieldsValidationException getIllegalCreationModeException() {
        ErrorResponseMessage message = new ErrorResponseMessage();
        message.add("creationMode", PageCRUDErrorMessages.ILLEGAL_CREATION_MODE.getErrorMessage());
        return new RequestFieldsValidationException(message);
    }

}
