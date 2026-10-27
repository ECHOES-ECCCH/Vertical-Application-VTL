package pl.psnc.vtl.ocrtraining.web.transcription;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.ocrtraining.domain.common.exception.VtlException;
import pl.psnc.vtl.ocrtraining.domain.training.OcrTrainingExampleService;
import pl.psnc.vtl.ocrtraining.domain.training.OcrTrainingService;
import pl.psnc.vtl.ocrtraining.domain.training.dto.OcrCorrectionsBatch;

import java.util.List;

@RestController
@RequestMapping("/api/training/ocr")
@RequiredArgsConstructor
@Slf4j
class TranscriptionsController {

    private final OcrTrainingExampleService ocrTrainingExampleService;
    private final OcrTrainingService ocrTrainingService;

    @GetMapping("/languages")
    @Operation(summary = "Get list of languages available for training.")
    ResponseEntity<List<String>> getTrainableLanguages() {
        return ResponseEntity.ok(ocrTrainingService.getTrainableLanguages());
    }

    @PostMapping("/corrections")
    @Operation(summary = "Accept ocr correction.")
    ResponseEntity<Void> acceptOcrCorrection(@RequestBody OcrCorrectionsBatch ocrCorrection) throws VtlException {
        ocrTrainingExampleService.validateOcrCorrections(ocrCorrection);
        ocrTrainingExampleService.savePendingTrainingExample(ocrCorrection);
        log.info("OCR corrections acknowledged successfully");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/corrections/force-download")
    @Operation(summary = "Force download of enqueued ocr corrections.")
    ResponseEntity<Object> enforceCorrectionsDownload() {
        ocrTrainingExampleService.processPendingTrainingExamples();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/run")
    @Operation(summary = "Run ocr training procedure.")
    ResponseEntity<Object> runOcrTrainingProcedure(@RequestParam(name = "lang", required = false) String lang) throws VtlException {
        if (lang == null)
            ocrTrainingService.resolveAndStartTrainingProcedure();
        else
            ocrTrainingService.startTrainingProcedure(lang);

        return ResponseEntity.ok().build();
    }
}
