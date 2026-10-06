package pl.psnc.vtl.portal.controllers.healthcheck;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.vtl.portal.conf.startup.PortalStartupRunner;
import pl.psnc.vtl.portal.dto.BuildInfoCore;
import pl.psnc.vtl.portal.services.statics.StaticFilesService;

@RestController
@RequestMapping("/api/healthcheck")
//@Tag(name = "Healthcheck", description = "Application state")
@RequiredArgsConstructor
public class HealthCheckController {

    private final StaticFilesService service;

    @GetMapping(path = "/buildinfo", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Check buildinfo")
    public ResponseEntity<BuildInfoCore> getBuildInfo() {
        return ResponseEntity.ok(new BuildInfoCore());
    }

    @GetMapping(path = "/start-state", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Check if app is loaded")@ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "App initialized and running"),
            @ApiResponse(responseCode = "503", description = "App has not been started")
    })
    public ResponseEntity<Void> getStartState() {
        if (PortalStartupRunner.isAppLoaded())
            return ResponseEntity.noContent().build();
        else return ResponseEntity.status(503).build();
    }


}
