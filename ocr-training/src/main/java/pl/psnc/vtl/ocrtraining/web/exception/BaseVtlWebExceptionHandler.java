package pl.psnc.vtl.ocrtraining.web.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pl.psnc.vtl.ocrtraining.domain.common.exception.VtlException;

import java.time.LocalDateTime;


@Slf4j
public abstract class BaseVtlWebExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    protected ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        log.error("General unexpected error", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(makeErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    protected ResponseEntity<ErrorResponse> handleVtlException(VtlException ex) {
        log.error("Unexpected Vtl exception", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(makeErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    protected ErrorResponse makeErrorResponse(Exception ex, HttpStatus status) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(ex.getMessage())
                .build();
    }
}
