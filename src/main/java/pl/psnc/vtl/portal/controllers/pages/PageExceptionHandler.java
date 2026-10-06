package pl.psnc.vtl.portal.controllers.pages;

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
import pl.psnc.vtl.portal.services.pages.BadRequestException;
import pl.psnc.vtl.portal.services.pages.PageContentImportException;
import pl.psnc.vtl.portal.services.parsers.ocr.HOCRParserException;
import pl.psnc.vtl.portal.services.parsers.tei.TEIParserException;

import java.time.LocalDateTime;
import java.util.Collections;

@ControllerAdvice(assignableTypes = {PageController.class, ContentImportController.class})
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PageExceptionHandler {

    @ExceptionHandler(value = { HOCRParserException.class, TEIParserException.class, BadRequestException.class })
    public ResponseEntity<Object> handleBadRequestException(I18nException ex) {
        log.error("Exception", ex);
        ErrorMessage errorMessage = ErrorMessage.builder().code(ex.getMessageCode()).build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    @ExceptionHandler(value = { PageContentImportException.class})
    public ResponseEntity<Object> handlePageContentImportException(PageContentImportException ex) {
        log.error("I18N Exception", ex);
        ErrorMessage errorMessage = ErrorMessage.builder().code(ex.getMessageCode())
                .params(ex.getFilename() != null ? Collections.singletonList(ex.getFilename()) : null).build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<Object> handle(Exception ex) {
        log.error("Exception", ex);
        ErrorResponse errorResponse =
                ErrorResponse
                        .builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Incorrect request parameter")
                        .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

}
