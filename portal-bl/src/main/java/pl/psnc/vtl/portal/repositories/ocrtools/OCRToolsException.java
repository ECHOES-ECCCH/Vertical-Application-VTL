package pl.psnc.vtl.portal.repositories.ocrtools;

import lombok.Getter;
import pl.psnc.vtl.portal.controllers.ErrorResponse;
import pl.psnc.vtl.portal.repositories.BackendSystemException;

@Getter
public class OCRToolsException extends BackendSystemException {

    public OCRToolsException(String message, ErrorResponse errorResponse) {
        super(message, errorResponse);
    }

    public OCRToolsException(String message, ErrorResponse errorResponse, Throwable cause) {
        super(message, errorResponse, cause);
    }

}
