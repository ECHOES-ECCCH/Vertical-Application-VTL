package pl.psnc.vtl.portal.controllers.layers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.psnc.vtl.portal.controllers.ErrorMessage;
import pl.psnc.vtl.portal.services.I18nException;
import pl.psnc.vtl.portal.services.verses.UndoRedoException;

@ControllerAdvice(assignableTypes = { PageLayerController.class, UndoRedoController.class })
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class LayerExceptionHandler {

    @ExceptionHandler(value = { UndoRedoException.class })
    public ResponseEntity<Object> handleUndoRedoException(I18nException ex) {
        log.error("I18N Exception", ex);
        ErrorMessage errorMessage = ErrorMessage.builder().code(ex.getMessageCode()).build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
    }
}
