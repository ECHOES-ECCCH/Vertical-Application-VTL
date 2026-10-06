package pl.psnc.vtl.ocr.api.ocr.tesseract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Ocr batch response object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrBatchResponse {

    /**
     * Map containgng verse id and verse content from ocr.
     */
    private Map<Long, String> versesContent;
}
