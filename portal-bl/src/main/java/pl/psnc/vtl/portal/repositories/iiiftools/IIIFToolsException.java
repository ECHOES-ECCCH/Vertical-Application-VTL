package pl.psnc.vtl.portal.repositories.iiiftools;

import pl.psnc.vtl.portal.controllers.ErrorResponse;
import pl.psnc.vtl.portal.repositories.BackendSystemException;

public class IIIFToolsException extends BackendSystemException {

    public IIIFToolsException(String message, ErrorResponse errorResponse) {
        super(message, errorResponse);
    }

    public IIIFToolsException(String message, ErrorResponse errorResponse, Throwable cause) {
        super(message, errorResponse, cause);
    }

}
