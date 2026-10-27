package pl.psnc.vtl.ocr.api.ocr.tesseract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Verse bounding box dto object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerseBboxDto {

    /**
     * Verse id.
     */
    private long id;

    /**
     * X1
     */
    private int x1;

    /**
     * X2
     */
    private int x2;

    /**
     * Y1
     */
    private int y1;

    /**
     * Y2
     */
    private int y2;
}
