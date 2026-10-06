package pl.psnc.vtl.ocr.domain.ocr.exception;

import pl.psnc.vtl.ocr.api.error.ErrorCode;
import pl.psnc.vtl.ocr.api.error.VtlOcrException;
import pl.psnc.vtl.ocr.api.ocr.tesseract.VerseBboxDto;

public class OcrRecognitionException extends VtlOcrException {

    public OcrRecognitionException(String imageFileName, int page, VerseBboxDto imageRegion) {
        super(ErrorCode.OCR_RECOGNITION_FAILURE, imageFileName, page, formatRegion(imageRegion));
    }

    public OcrRecognitionException(String imageFileName, int page, VerseBboxDto imageRegion, Throwable cause) {
        super(ErrorCode.OCR_RECOGNITION_FAILURE, cause, imageFileName, page, formatRegion(imageRegion));
    }

    private static String formatRegion(VerseBboxDto imageRegion) {
        if (imageRegion == null)
            return "[full]";

        return String.format(
                "[(%d, %d) -> (%d, %d)]",
                imageRegion.getX1(),
                imageRegion.getY1(),
                imageRegion.getX2(),
                imageRegion.getY2()
        );
    }
}
