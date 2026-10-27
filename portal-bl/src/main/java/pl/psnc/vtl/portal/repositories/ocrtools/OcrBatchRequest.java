package pl.psnc.vtl.portal.repositories.ocrtools;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrBatchRequest {
    private List<OcrBoundingBox> verses;
}
