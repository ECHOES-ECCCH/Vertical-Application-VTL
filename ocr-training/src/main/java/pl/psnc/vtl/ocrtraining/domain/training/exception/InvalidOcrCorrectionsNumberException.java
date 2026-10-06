package pl.psnc.vtl.ocrtraining.domain.training.exception;

import pl.psnc.vtl.ocrtraining.domain.common.exception.ErrorCode;
import pl.psnc.vtl.ocrtraining.domain.common.exception.VtlException;


public class InvalidOcrCorrectionsNumberException extends VtlException {

    public InvalidOcrCorrectionsNumberException() {
        super(ErrorCode.API_OCR_CORRECTION_INVALID_NUMBER);
    }
}
