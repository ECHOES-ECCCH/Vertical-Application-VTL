package pl.psnc.vtl.portal.controllers.ocrmodels;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.vtl.portal.model.ocrmodels.OcrModel;
import pl.psnc.vtl.portal.services.ocrmodels.OcrModelService;

import java.util.List;

@RestController
@RequestMapping("/api/ocrmodels")
@Validated
@RequiredArgsConstructor
@Tag(name = "OCR models")
public class OcrModelsController {

    private final OcrModelService ocrModelService;

    @GetMapping
    public ResponseEntity<List<OcrModel>> getOcrModels() {
        return ResponseEntity.ok(ocrModelService.getOcrModels());
    }
}
