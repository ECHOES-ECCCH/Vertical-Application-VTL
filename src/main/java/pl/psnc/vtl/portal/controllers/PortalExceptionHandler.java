package pl.psnc.vtl.portal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import pl.psnc.vtl.portal.repositories.BackendSystemException;
import pl.psnc.vtl.portal.repositories.iiifserver.IIIFServerException;
import pl.psnc.vtl.portal.repositories.ocrtools.OCRToolsException;
import pl.psnc.vtl.portal.services.ValidationException;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionProcessingInProgressException;

import javax.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class PortalExceptionHandler {

    @ExceptionHandler(value = TranscriptionProcessingInProgressException.class)
    public ResponseEntity<Object> handleProcessingInProgress(TranscriptionProcessingInProgressException ex) {
        log.debug("Operation in progress", ex);
        ErrorMessage errorMessage = ErrorMessage.builder().code(ex.getMessageCode()).build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
    }

    @ExceptionHandler(value = { MaxUploadSizeExceededException.class })
    public ResponseEntity<Object> handleMaxFileSize(MaxUploadSizeExceededException ex) {
        log.error("Max file size exceeded", ex);
        ErrorMessage errorMessage = ErrorMessage.builder().code("pages.error.fileTooLarge")
                .params(Collections.singletonList(ex.getMessage())).build();
        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(value = { EntityNotFoundException.class, NoHandlerFoundException.class })
    public ResponseEntity<Object> handleNotFoundException(Exception ex) {
        log.error("Entity not found", ex);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value()).error(ex.getMessage()).build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<Object> handleForbiddenException(AccessDeniedException ex) {
        log.error("Forbidden", ex);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value()).error(ex.getMessage()).build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(value = { OCRToolsException.class, IIIFServerException.class })
    public ResponseEntity<Object> handleBackendSystemException(BackendSystemException ex) {
        log.error("Backend system error", ex);
        return ResponseEntity.status(ex.getErrorResponse().getStatus()).body(ex.getErrorResponse());
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleServerException(Exception ex) {
        log.error("Server Exception", ex);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(ex.getMessage()).build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(value = { RequestFieldsValidationException.class })
    public ResponseEntity<Object> handleRequestValidationException(RequestFieldsValidationException ex) {
        log.debug("Request validation exception", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getErrorResponseMessage().getMessages());
    }

    @ExceptionHandler(value = ValidationException.class)
    public ResponseEntity<Object> handleValidationException(ValidationException ex) {
        log.debug("Validation exception", ex);
        Map<String, String> errors = ex.getErrors().getFieldErrors().stream().map(error -> Pair.create(error.getField(), error.getCode()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue, (u, v) -> u, LinkedHashMap::new));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(value = { javax.validation.ValidationException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class, HttpRequestMethodNotSupportedException.class, })
    public ResponseEntity<Object> handleBadRequastException(Exception ex) {
        log.debug("Bad request", ex);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value()).error(ex.getMessage()).build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
