package pl.psnc.vtl.ocr.api.ocr.tesseract;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.ocr.api.error.VtlOcrException;
import pl.psnc.vtl.ocr.domain.ocr.TesseractService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class TesseractController {

    private final TesseractService tesseractService;

    @GetMapping("/languages")
    @Operation(summary = "Get supported languages.")
    public ResponseEntity<List<String>> getTrainedLanguages() {
        return ResponseEntity.ok(tesseractService.getTrainedLanguages());
    }

    @GetMapping("/{transcriptionId}/recognize-hocr/{filename}")
    @Operation(summary = "Recognize hocr.")
    public ResponseEntity<String> recognizeHocr(@PathVariable("transcriptionId") long transcriptionId,
            @PathVariable String filename,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "eng") String lang,
            @RequestParam(required = false, defaultValue = "false") boolean handleColumns)
            throws VtlOcrException {

        String hocr = tesseractService.scanImageHocr(transcriptionId, filename, page, lang, handleColumns);

        return ResponseEntity.ok(hocr);
    }

    @PostMapping(value = "/{transcriptionId}/recognize-subimg-batch/{filename}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Recognize subimage batch.")
    public ResponseEntity<OcrBatchResponse> recognizeSubimageBatch(
            @PathVariable("transcriptionId") long transcriptionId, @PathVariable("filename") String filename,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "eng") String lang,
            @RequestParam(required = false, defaultValue = "false") boolean hocr,
            @RequestParam(required = false, defaultValue = "false") boolean singleWord,
            @RequestBody VerseBboxesDto ocrRequest)
            throws VtlOcrException {

        Map<Long, String> versesContent =
                tesseractService.scanSubImageBatchPlain(transcriptionId, filename, page, ocrRequest, lang, hocr,
                        singleWord);

        return ResponseEntity.ok(new OcrBatchResponse(versesContent));
    }
}
