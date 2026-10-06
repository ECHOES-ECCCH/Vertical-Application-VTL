package pl.psnc.vtl.portal.controllers.verses;

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
import pl.psnc.vtl.portal.services.I18nException;
import pl.psnc.vtl.portal.services.verses.IncorrectPositionException;
import pl.psnc.vtl.portal.services.verses.VerseAlreadyMarkedVerifiedException;

import java.time.LocalDateTime;
import java.util.Collections;

@ControllerAdvice(assignableTypes = { VerseContentController.class, VerseController.class, CommentController.class })
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class VerseExceptionHandler{

    @ExceptionHandler(value = { IncorrectPositionException.class, VerseAlreadyMarkedVerifiedException.class })
    public ResponseEntity<Object> handleBadRequestException(I18nException ex) {
        log.error("I18N Exception", ex);
        ErrorMessage errorMessage = ErrorMessage.builder()
                .code(ex.getMessageCode())
                .params(Collections.singletonList(ex.getMessage()))
                .build();
        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(value = {InvalidFormatException.class, JsonParseException.class})
    public ResponseEntity<ErrorResponse> handleInvalidFormatException(Exception exception) {
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value()).error("Request payload is invalid. " + exception.getMessage()).build();
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
