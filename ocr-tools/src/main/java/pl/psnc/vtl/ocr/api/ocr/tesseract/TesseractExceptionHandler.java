package pl.psnc.vtl.ocr.api.ocr.tesseract;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.psnc.vtl.ocr.api.error.ErrorResponse;
import pl.psnc.vtl.ocr.api.error.VtlOcrException;
import pl.psnc.vtl.ocr.api.error.base.BaseVtlExceptionHandler;


@RestControllerAdvice(assignableTypes = { TesseractController.class })
@Slf4j
class TesseractExceptionHandler extends BaseVtlExceptionHandler {

    @ExceptionHandler(VtlOcrException.class)
    @Override
    protected ResponseEntity<ErrorResponse> handleOcrException(VtlOcrException e) {

        switch (e.getErrorCode()) {
            case TRANSCRIPTION_IMAGE_NOT_FOUND:
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(makeErrorResponse(e, HttpStatus.NOT_FOUND));

            case TRANSCRIPTION_PAGE_INVALID:
            case OCR_LANG_NOT_SUPPORTED:
                return ResponseEntity.badRequest().body(makeErrorResponse(e, HttpStatus.BAD_REQUEST));

            case TRANSCRIPTION_IMAGE_FAILED_TO_LOAD:
            case OCR_RECOGNITION_FAILURE:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(makeErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR));

            default:
                return super.handleOcrException(e);
        }
    }
}
