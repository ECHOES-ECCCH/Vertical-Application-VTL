package pl.psnc.vtl.portal.controllers.imports;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.psnc.vtl.portal.controllers.ErrorMessage;
import pl.psnc.vtl.portal.repositories.iiiftools.FailedToSavePagesException;

@ControllerAdvice(assignableTypes = { ImportsController.class })
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ImportsExceptionHandler {

    @ExceptionHandler(value = { FailedToSavePagesException.class })
    public ResponseEntity<Object> handleFailedToSavePagesException(FailedToSavePagesException ex) {
        log.error("I18N Exception", ex);
        ErrorMessage errorMessage = ErrorMessage.builder().code(ex.getMessageCode()).build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

}
