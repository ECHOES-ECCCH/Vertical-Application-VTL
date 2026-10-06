package pl.psnc.vtl.ocrtraining.domain.training.exception;

import pl.psnc.vtl.ocrtraining.domain.common.exception.ErrorCode;
import pl.psnc.vtl.ocrtraining.domain.common.exception.VtlException;

import java.util.List;


public class UnsupportedOcrLanguageException extends VtlException {

    public UnsupportedOcrLanguageException(List<String> unsupportedLangs) {
        super(ErrorCode.API_OCR_CORRECTION_UNSUPPORTED_LANG, formatUnsupportedLangs(unsupportedLangs));
    }

    private static String formatUnsupportedLangs(List<String> unsupportedLangs) {
        return String.join(",", unsupportedLangs);
    }
}
