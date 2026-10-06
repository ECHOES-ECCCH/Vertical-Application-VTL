package pl.psnc.vtl.portal.repositories;

import lombok.Getter;
import pl.psnc.vtl.portal.controllers.ErrorResponse;

@Getter
public abstract class BackendSystemException extends RuntimeException {

    protected ErrorResponse errorResponse;

    public BackendSystemException(String message, ErrorResponse errorResponse) {
        super(message);
        this.errorResponse = errorResponse;
    }

    public BackendSystemException(String message, ErrorResponse errorResponse, Throwable cause) {
        super(message, cause);
        this.errorResponse = errorResponse;
    }

}
