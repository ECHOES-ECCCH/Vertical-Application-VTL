package pl.psnc.vtl.ocrtraining.domain.training.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;


/**
 * Ocr correction object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrCorrection {

    /**
     * Image url in iiif format.
     */
    private String iiifImageUrl;

    /**
     * Correct text.
     */
    private String correctText;

    /**
     * Text language.
     */
    private String lang;
}
