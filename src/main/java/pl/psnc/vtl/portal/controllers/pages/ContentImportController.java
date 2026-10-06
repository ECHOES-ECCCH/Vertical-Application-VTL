package pl.psnc.vtl.portal.controllers.pages;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.services.pages.ContentImportService;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
class ContentImportController {

    private final ContentImportService contentImportService;

    @Operation(summary = "Import pages (without metadata) to given transcription form provided IIIF source.")
    @PostMapping(path = "/transcription/{transcriptionId}/import-iiif")
    public ResponseEntity<Void> importPagesFromIIIF(
            @RequestBody @RequestParam("resourceURL") String resourceURL,
            @PathVariable("transcriptionId") long transcriptionId) throws ConfigurationException {
        contentImportService.importPagesFromIIIF(transcriptionId, resourceURL);
        return ResponseEntity.ok().build();
    }
}
