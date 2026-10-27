package pl.psnc.vtl.portal.repositories.ocrtraining;

import lombok.Getter;
import pl.psnc.vtl.portal.controllers.ErrorResponse;
import pl.psnc.vtl.portal.repositories.BackendSystemException;

@Getter
public class OCRTrainingException extends BackendSystemException {

    public OCRTrainingException(String message, ErrorResponse errorResponse) {
        super(message, errorResponse);
    }

    public OCRTrainingException(String message, ErrorResponse errorResponse, Throwable cause) {
        super(message, errorResponse, cause);
    }

}