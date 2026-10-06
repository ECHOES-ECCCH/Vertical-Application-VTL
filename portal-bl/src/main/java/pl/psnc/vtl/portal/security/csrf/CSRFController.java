package pl.psnc.vtl.portal.security.csrf;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
@Tag(name ="Security")
@RequiredArgsConstructor
public class CSRFController {

    private static final String CSRF_TOKEN = "CSRF-TOKEN";
    private final CSRFTokenService service;

    @Operation(summary = "get csrf token")
    @GetMapping("/auth/csrf/token")
    ResponseEntity<Void> getToken(HttpServletRequest request,
                                  HttpServletResponse response){
        service.generateCSRFToken(request, response);
        return ResponseEntity.ok().build();
    }
}

