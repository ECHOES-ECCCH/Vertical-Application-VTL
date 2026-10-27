package pl.psnc.vtl.ocr.api.ocr.tesseract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Verse bounding boxes in OCR batch request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerseBboxesDto {

    /**
     * List of verse voundeing boxes.
     */
    private List<VerseBboxDto> verses;
}
