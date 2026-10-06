package pl.psnc.vtl.ocr.domain.ocr.exception;

import pl.psnc.vtl.ocr.api.error.ErrorCode;
import pl.psnc.vtl.ocr.api.error.VtlOcrException;

public class InvalidTranscriptionPageException extends VtlOcrException {

    public InvalidTranscriptionPageException(String imageFileName, int pageNo) {
        super(ErrorCode.TRANSCRIPTION_PAGE_INVALID, imageFileName, pageNo);
    }

    public InvalidTranscriptionPageException(String imageFileName, int pageNo, Throwable cause) {
        super(ErrorCode.TRANSCRIPTION_PAGE_INVALID, cause, imageFileName, pageNo);
    }
}
