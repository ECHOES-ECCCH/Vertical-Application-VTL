package pl.psnc.vtl.portal.controllers.ocr;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.model.transcriptions.Language;
import pl.psnc.vtl.portal.services.parsers.ocr.OCRToolsService;

import java.util.List;

@RestController
@RequestMapping("/api/ocr")
@Tag(name = "OCRTools")
@RequiredArgsConstructor
public class OCRToolsController {

    private final OCRToolsService ocrToolsService;

    @Operation(summary = "Send verses from given layer to train Tesseract")
    @PutMapping(path = "/train/layer/{layerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> trainTesseract(@PathVariable("layerId") Long layerId,
            @RequestParam("language") String language,
            @RequestBody(required = false) List<PageRange> pageRanges) {
        int examplesCount = ocrToolsService.trainTesseractFromLayer(layerId, pageRanges, language);
        return ResponseEntity.ok(examplesCount);
    }

    @Operation(summary = "Retrieves list of supported OCR languages")
    @GetMapping(path = "/languages")
    public ResponseEntity<List<Language>> getLanguages() {
        return ResponseEntity.ok(ocrToolsService.getOcrLanguages());
    }

    @Operation(summary = "Retrieves list of languages that can be trained")
    @GetMapping(path = "/train/languages")
    public ResponseEntity<List<Language>> getTrainLanguages() {
        return ResponseEntity.ok(ocrToolsService.getTrainLanguages());
    }
}
