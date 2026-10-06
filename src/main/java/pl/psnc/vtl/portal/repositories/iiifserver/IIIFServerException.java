package pl.psnc.vtl.portal.repositories.iiifserver;

import pl.psnc.vtl.portal.controllers.ErrorResponse;
import pl.psnc.vtl.portal.repositories.BackendSystemException;

public class IIIFServerException extends BackendSystemException {

    public IIIFServerException(String message, ErrorResponse errorResponse) {
        super(message, errorResponse);
    }

    public IIIFServerException(String message, ErrorResponse errorResponse, Throwable cause) {
        super(message, errorResponse, cause);
    }

}
