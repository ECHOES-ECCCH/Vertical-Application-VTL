package pl.psnc.vtl.ocrtraining.web.transcription;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.psnc.vtl.ocrtraining.domain.common.exception.VtlException;
import pl.psnc.vtl.ocrtraining.web.exception.BaseVtlWebExceptionHandler;
import pl.psnc.vtl.ocrtraining.web.exception.ErrorResponse;

@RestControllerAdvice(assignableTypes = { TranscriptionsController.class })
@Slf4j
class TranscriptionExceptionHandler extends BaseVtlWebExceptionHandler {

    @ExceptionHandler(VtlException.class)
    protected ResponseEntity<ErrorResponse> handleVtlException(VtlException ex) {
        switch (ex.getErrorCode()) {
            default:
                return super.handleVtlException(ex);
        }
    }
}
