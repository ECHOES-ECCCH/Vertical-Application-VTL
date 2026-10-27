package pl.psnc.vtl.portal.controllers.transcriptions;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.psnc.vtl.portal.controllers.ErrorMessage;
import pl.psnc.vtl.portal.controllers.ErrorResponse;
import pl.psnc.vtl.portal.repositories.iiiftools.IIIFToolsException;
import pl.psnc.vtl.portal.services.I18nException;
import pl.psnc.vtl.portal.services.pages.WrongFileTypeException;
import pl.psnc.vtl.portal.services.transcriptions.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@ControllerAdvice(assignableTypes = { TranscriptionController.class, TranscriptionPrivilegesController.class, TranscriptionStatisticsController.class })
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class TranscriptionExceptionHandler {

    @ExceptionHandler(value = { WrongFileTypeException.class })
    public ResponseEntity<Object> handleWrongFileTypeException(WrongFileTypeException ex) {
        log.error("I18N Exception", ex);
        ErrorMessage errorMessage = ErrorMessage.builder().code(ex.getMessageCode())
                .params(ex.getFilename() != null ? Collections.singletonList(ex.getFilename()) : null).build();
        return ResponseEntity.badRequest().body(errorMessage);
    }


    @ExceptionHandler(value = {
            TranscriptionUserAlreadyExistsException.class,
            NoUserWithGivenEmailException.class,
            TranscriptionCreatorRemovalException.class,
            GroupAlreadyExistsInTranscriptionException.class,
            ParsePagesRangeException.class,
            NoSuchPageException.class })
    public ResponseEntity<Object> handleI18NExceptions(I18nException ex) {
        log.error("I18N Exception", ex);
        ErrorMessage errorMessage = ErrorMessage.builder().code(ex.getMessageCode()).build();
        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(value = { JsonParseException.class, InvalidFormatException.class })
    public ResponseEntity<ErrorResponse> handleJsonExceptions(Exception e) {
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value()).error("Request payload is invalid. " + e.getMessage()).build();
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(value = { PageTooLargeException.class })
    public ResponseEntity<Object> handleRuntimeBadRequestException(I18nException ex) {
        log.error("Exception", ex);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value()).error(ex.getMessageCode()).build();
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(value = { GroupNotFoundException.class })
    public ResponseEntity<Object> handleNotFoundException(I18nException ex) {
        log.error("Exception", ex);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value()).error(ex.getMessageCode()).build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(value = {IIIFToolsException.class})
    public ResponseEntity<Object> handleIIIFToolsException(IIIFToolsException ex) {
        log.error("IIIF Tools Exception", ex);
        ErrorMessage errorMessage = ErrorMessage.builder().code("pages.error.iiif-tools")
                .params(List.of(ex.getMessage())).build();
        return ResponseEntity.badRequest().body(errorMessage);
    }
}
