package pl.psnc.vtl.portal.controllers.statics;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.vtl.portal.dto.statics.StaticFile;
import pl.psnc.vtl.portal.services.statics.StaticFilesService;

@RestController
@RequestMapping("/api/static-files")
@RequiredArgsConstructor
@Tag(name = "Static files")
public class StaticFilesController {

    private final StaticFilesService service;

    @GetMapping(path = "{fileName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StaticFile> getPage(@PathVariable("fileName") String fileName) {
        if (!fileName.matches("[a-zA-Z0-9-_]+")) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.loadFile(fileName));
    }

    @GetMapping(path = "/images/{fileName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource> getStaticImage(@PathVariable("fileName") String fileName) {
        if (!fileName.matches("[a-zA-Z0-9-_]+\\.[a-zA-Z0-9]+")) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok()
                .contentType(service.getMediaType(fileName))
                .body(service.loadImage(fileName));
    }
}
