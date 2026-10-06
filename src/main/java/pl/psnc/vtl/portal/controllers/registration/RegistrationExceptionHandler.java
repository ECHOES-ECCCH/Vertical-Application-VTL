package pl.psnc.vtl.portal.controllers.registration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.psnc.vtl.portal.controllers.ErrorMessage;
import pl.psnc.vtl.portal.services.I18nException;
import pl.psnc.vtl.portal.services.registration.*;

@ControllerAdvice(assignableTypes = {RegistrationController.class, PasswordController.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RegistrationExceptionHandler{

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = { ExpiredTokenException.class, InvalidTokenException.class,
            EmailDoesNotExistException.class, PasswordValidationException.class})
    public ResponseEntity<Object> handleBadRequestException(I18nException ex) {
        log.error("I18N Exception", ex);
        ErrorMessage errorMessage = ErrorMessage.builder().code(ex.getMessageCode()).build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = {BadCredentialsException.class,})
    public ResponseEntity<Object> handleBadCredentialsException(Exception ex) {
        log.error("BadCredentialsException", ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ExceptionHandler(value = { InvalidUserDataException.class})
//    public ResponseEntity<Object> handleInvalidUserDataException(InvalidUserDataException e) {
//        log.error("InvalidUserDataException", e);
//        return ResponseEntity.badRequest().body(e.getDetailedMessage().getMessages());
//    }

}
