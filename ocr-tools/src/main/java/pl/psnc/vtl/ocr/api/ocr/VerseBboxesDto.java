package pl.psnc.vtl.ocr.api.ocr;

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerseBboxDto {

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
}
