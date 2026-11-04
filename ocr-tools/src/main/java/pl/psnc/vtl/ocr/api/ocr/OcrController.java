package pl.psnc.vtl.ocr.api.ocr;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.ocr.api.error.VtlOcrException;
import pl.psnc.vtl.ocr.domain.ocr.OcrService;

import java.util.Map;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService tesseractService;

    @GetMapping("/{transcriptionId}/recognize-hocr/{filename}")
    @Operation(summary = "Recognize hocr.")
    public ResponseEntity<String> recognizeHocr(@PathVariable("transcriptionId") long transcriptionId,
            @PathVariable String filename,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = true) String engine,
            @RequestParam(required = true) String model,
            @RequestParam(required = false, defaultValue = "false") boolean handleColumns)
            throws VtlOcrException {

        String hocr = tesseractService.scanImageHocr(transcriptionId, filename, page, engine, model, handleColumns);

        return ResponseEntity.ok(hocr);
    }

    @PostMapping(value = "/{transcriptionId}/recognize-subimg-batch/{filename}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Recognize subimage batch.")
    public ResponseEntity<OcrBatchResponse> recognizeSubimageBatch(
            @PathVariable("transcriptionId") long transcriptionId, @PathVariable("filename") String filename,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = true) String engine,
            @RequestParam(required = true) String model,
            @RequestParam(required = false, defaultValue = "false") boolean hocr,
            @RequestParam(required = false, defaultValue = "false") boolean singleWord,
            @RequestBody VerseBboxesDto ocrRequest)
            throws VtlOcrException {

        Map<Long, String> versesContent =
                tesseractService.scanSubImageBatchPlain(transcriptionId, filename, page, ocrRequest, engine, model,
                        hocr, singleWord);

        return ResponseEntity.ok(new OcrBatchResponse(versesContent));
    }
}
