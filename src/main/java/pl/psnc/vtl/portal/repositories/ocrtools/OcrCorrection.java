package pl.psnc.vtl.portal.repositories.ocrtools;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OcrCorrection {

    private String iiifImageUrl;
    private String correctText;
    private String lang;
}