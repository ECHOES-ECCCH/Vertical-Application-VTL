package pl.psnc.vtl.portal.controllers.properties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.psnc.vtl.portal.controllers.ErrorResponse;
import pl.psnc.vtl.portal.services.properties.LoadPropertiesException;

import java.time.LocalDateTime;

@ControllerAdvice(assignableTypes = { PropertyController.class })
@Slf4j
public class PropertyExceptionHandler{

    @ExceptionHandler(value = { LoadPropertiesException.class })
    public ResponseEntity<Object> handleRuntimeInternalErrorException(Exception ex) {
        log.error("Exception", ex);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(ex.getMessage()).build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

}
