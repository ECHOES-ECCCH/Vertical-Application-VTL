package pl.psnc.vtl.ocr.api.error.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pl.psnc.vtl.ocr.api.error.ErrorResponse;
import pl.psnc.vtl.ocr.api.error.VtlOcrException;

import java.time.LocalDateTime;


@Slf4j
public class BaseVtlExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(VtlOcrException.class)
    protected ResponseEntity<ErrorResponse> handleOcrException(VtlOcrException e) {
        switch (e.getErrorCode()) {
            default:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(makeErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleServerException(Exception ex) {
        log.error("Server Exception", ex);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(ex.getMessage()).build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleServerError(Exception ex) {
        log.error("Runtime exception", ex);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(ex.getMessage()).build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    protected ErrorResponse makeErrorResponse(VtlOcrException e, HttpStatus status) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(e.getMessage())
                .build();
    }
}
