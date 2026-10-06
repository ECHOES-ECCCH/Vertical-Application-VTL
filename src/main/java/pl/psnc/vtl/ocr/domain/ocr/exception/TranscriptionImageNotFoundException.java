package pl.psnc.vtl.ocr.domain.ocr.exception;

import pl.psnc.vtl.ocr.api.error.ErrorCode;
import pl.psnc.vtl.ocr.api.error.VtlOcrException;

public class TranscriptionImageNotFoundException extends VtlOcrException {


    public TranscriptionImageNotFoundException(long transcriptionId, String imageFileName) {
        super(ErrorCode.TRANSCRIPTION_IMAGE_NOT_FOUND, transcriptionId, imageFileName);
    }

    public TranscriptionImageNotFoundException(long transcriptionId, String imageFileName, Throwable cause) {
        super(ErrorCode.TRANSCRIPTION_IMAGE_NOT_FOUND, cause, transcriptionId, imageFileName);
    }
}
