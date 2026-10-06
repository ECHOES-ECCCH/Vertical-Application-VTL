package pl.psnc.vtl.iiif.eBooks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.psnc.vtl.iiif.controllers.ErrorResponse;
import pl.psnc.vtl.iiif.controllers.image.IIIFServerController;
import pl.psnc.vtl.iiif.converters.ConversionException;
import pl.psnc.vtl.iiif.services.image.NameCannotBeExtractedException;

import java.time.LocalDateTime;

@ControllerAdvice(assignableTypes = { EBookConvertersController.class})
@Slf4j
public class EBookConvertersExceptionHandler {

    @ExceptionHandler(value = ConversionException.class)
    public ResponseEntity handleConversionException(Exception ex) {
        log.error("Exception", ex);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(ex.getMessage()).build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(value = NameCannotBeExtractedException.class)
    public ResponseEntity handleNameExtractionException(NameCannotBeExtractedException ex) {
        log.error("Exception", ex);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value()).error(ex.getMessage()).build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

}
