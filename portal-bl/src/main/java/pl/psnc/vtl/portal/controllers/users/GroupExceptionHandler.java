package pl.psnc.vtl.portal.controllers.users;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.psnc.vtl.portal.controllers.ErrorMessage;
import pl.psnc.vtl.portal.services.I18nException;
import pl.psnc.vtl.portal.services.users.NoUserWithGivenEmailException;
import pl.psnc.vtl.portal.services.users.GroupAlreadyExistsException;
import pl.psnc.vtl.portal.services.users.GroupCreatorRemovalException;
import pl.psnc.vtl.portal.services.users.UserGroupAlreadyExsitsException;

@ControllerAdvice(assignableTypes = { GroupController.class })
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class GroupExceptionHandler {

    @ExceptionHandler(value = { GroupAlreadyExistsException.class, NoUserWithGivenEmailException.class,
            UserGroupAlreadyExsitsException.class, GroupCreatorRemovalException.class })
    public ResponseEntity<Object> handleBadRequestException(I18nException ex) {
        log.error("I18N Exception", ex);
        ErrorMessage errorMessage = ErrorMessage.builder().code(ex.getMessageCode()).build();
        return ResponseEntity.badRequest().body(errorMessage);
    }

}
