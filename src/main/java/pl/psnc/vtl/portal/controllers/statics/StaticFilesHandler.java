package pl.psnc.vtl.portal.controllers.statics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.psnc.vtl.portal.controllers.ErrorMessage;
import pl.psnc.vtl.portal.repositories.statics.StaticFileNotFoundException;

@ControllerAdvice(assignableTypes = StaticFilesController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class StaticFilesHandler {

    @ExceptionHandler(value = { StaticFileNotFoundException.class })
    public ResponseEntity<Object> handleStaticFileNotFound(StaticFileNotFoundException ex) {
        log.error("Static files Exception", ex);
        ErrorMessage errorMessage = ErrorMessage.builder().code(ex.getMessage()).build();
        return ResponseEntity.status(422).body(errorMessage);
    }
}
