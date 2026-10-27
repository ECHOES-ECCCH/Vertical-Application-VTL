package pl.psnc.vtl.portal.controllers.transcriptions;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.vtl.portal.model.transcriptions.Language;
import pl.psnc.vtl.portal.services.transcriptions.LanguageService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Language")
public class LanguageController {

    private final LanguageService languageService;

    @Operation(summary = "Retrieve all languages that can be used in transcription metadata")
    @GetMapping(path = "/languages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Language>> getLanguages() {
        return ResponseEntity.ok(languageService.getLanguages());
    }

}
