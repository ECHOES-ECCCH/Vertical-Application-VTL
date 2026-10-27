package pl.psnc.vtl.portal.model.users;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.psnc.vtl.portal.controllers.ErrorMessage;
import pl.psnc.vtl.portal.controllers.admin.AdminController;
import pl.psnc.vtl.portal.controllers.imports.ImportsController;
import pl.psnc.vtl.portal.controllers.transcriptions.TranscriptionController;

@ControllerAdvice(assignableTypes = { TranscriptionController.class, AdminController.class, ImportsController.class })
@Order(Ordered.HIGHEST_PRECEDENCE)
public class QuotaExceptionHandler {

    @ExceptionHandler(value = { QuotaException.class })
    public ResponseEntity<Object> handleQuotaException(QuotaException ex) {
        ErrorMessage errorMessage = ErrorMessage.builder().code(ex.getMessageCode()).build();
        return ResponseEntity.badRequest().body(errorMessage);
    }
}
