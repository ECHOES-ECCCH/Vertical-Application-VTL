package pl.psnc.vtl.ocr.domain.ocr.exception;

import pl.psnc.vtl.ocr.api.error.ErrorCode;
import pl.psnc.vtl.ocr.api.error.VtlOcrException;

public class TranscriptionImageLoadException extends VtlOcrException {

    public TranscriptionImageLoadException(String imageFileName, int page) {
        super(ErrorCode.TRANSCRIPTION_IMAGE_FAILED_TO_LOAD, imageFileName, page);
    }

    public TranscriptionImageLoadException(String imageFileName, int page, Throwable cause) {
        super(ErrorCode.TRANSCRIPTION_IMAGE_FAILED_TO_LOAD, cause, imageFileName, page);
    }
}
