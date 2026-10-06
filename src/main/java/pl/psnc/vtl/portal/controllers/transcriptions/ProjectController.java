package pl.psnc.vtl.portal.controllers.transcriptions;


import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.vtl.portal.dto.transcriptions.list.TranscriptionsForDaceDto;
import pl.psnc.vtl.portal.services.transcriptions.ProjectService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Project", description = "Transription API for module integration:  BAD, EDU and WWW")
@Slf4j
public class ProjectController {

    private final ProjectService projectService;


    @GetMapping(path = "/projects", params = {"daceId"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TranscriptionsForDaceDto> getProjects(
            @Parameter(required = true) @RequestParam String daceId) {
        return ResponseEntity.ok(
                TranscriptionsForDaceDto.builder().projects(projectService.getProjects(daceId)).build());
    }
}
