package pl.psnc.vtl.ocrtraining.domain.training.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;


/**
 * Ocr corrections batch object.
 */
@Data
public class OcrCorrectionsBatch {

    /**
     * List of ocr correction objects.
     */
    private final List<OcrCorrection> corrections;

    @JsonCreator
    public OcrCorrectionsBatch(@JsonProperty(value = "corrections", required = true) List<OcrCorrection> corrections) {
        this.corrections = corrections;
    }
}
