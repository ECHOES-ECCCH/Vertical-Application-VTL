package pl.psnc.vtl.iiif.controllers.healthcheck;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.vtl.iiif.services.image.IIIFServerService;

@RestController
@RequestMapping("/.well-known")
@Tag(name = "Healthcheck", description = "Application state")
@RequiredArgsConstructor
public class HealthCheckController {

    private final IIIFServerService iiifServerService;

    @GetMapping(path = "/live", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Check if app is alive")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "App   running")
    })
    public ResponseEntity<Void> getLiveState() {

        return ResponseEntity.status(200).build();
    }

    @GetMapping(path = "/ready", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Check if app is ready ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "App ready ")
    })
    public ResponseEntity<HealthCheckDto> getReadyState() {
        HealthCheckDto healthCheckDto = new HealthCheckDto();
        healthCheckDto.setBusy(iiifServerService.isBusy());
        healthCheckDto.setActiveTasks(iiifServerService.taskCounter());
        return ResponseEntity.ok(healthCheckDto);
    }


}
