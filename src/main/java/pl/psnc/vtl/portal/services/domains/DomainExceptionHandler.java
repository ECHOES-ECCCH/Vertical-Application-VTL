package pl.psnc.vtl.portal.services.domains;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.psnc.vtl.portal.controllers.ErrorMessage;
import pl.psnc.vtl.portal.controllers.domains.DomainController;
import pl.psnc.vtl.portal.controllers.imports.ImportsController;
import pl.psnc.vtl.portal.services.I18nException;

@Slf4j
@ControllerAdvice(assignableTypes = { DomainService.class, ImportsController.class, DomainController.class })
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DomainExceptionHandler {

    @ExceptionHandler(value = { DomainNotAvailableException.class, DomainNotAvailableForExternalEditException.class })
    public ResponseEntity<Object> handleI18NExceptions(I18nException ex) {
        log.error("I18N Exception", ex);
        ErrorMessage errorMessage = ErrorMessage.builder().code(ex.getMessageCode()).build();
        return ResponseEntity.badRequest().body(errorMessage);
    }


    @ExceptionHandler(value = { ConfigurationException.class })
    public ResponseEntity<Object> handleInternalServerErrorException(Exception ex) {
        log.error("Configuration exception: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(ex.getMessage());
    }
}
