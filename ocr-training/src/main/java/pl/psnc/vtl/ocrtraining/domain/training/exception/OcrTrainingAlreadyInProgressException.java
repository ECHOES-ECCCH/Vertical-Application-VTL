package pl.psnc.vtl.ocrtraining.domain.training.exception;

import pl.psnc.vtl.ocrtraining.domain.common.exception.ErrorCode;
import pl.psnc.vtl.ocrtraining.domain.common.exception.VtlException;

public class OcrTrainingAlreadyInProgressException extends VtlException {

    public OcrTrainingAlreadyInProgressException() {
        super(ErrorCode.OCR_TRAINING_ALREADY_IN_PROGRESS);
    }
}
