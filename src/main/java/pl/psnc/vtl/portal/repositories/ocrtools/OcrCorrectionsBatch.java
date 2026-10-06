package pl.psnc.vtl.portal.repositories.ocrtools;

import lombok.Data;

import java.util.List;

@Data
public class OcrCorrectionsBatch {

    private List<OcrCorrection> corrections;
}