package pl.psnc.vtl.ocr.domain.ocr.exception;

import pl.psnc.vtl.ocr.api.error.ErrorCode;
import pl.psnc.vtl.ocr.api.error.VtlOcrException;


public class OcrUnsupportedLanguageException extends VtlOcrException {

    public OcrUnsupportedLanguageException(String lang) {
        super(ErrorCode.OCR_LANG_NOT_SUPPORTED, lang);
    }

    public OcrUnsupportedLanguageException(String lang, Throwable cause) {
        super(ErrorCode.OCR_LANG_NOT_SUPPORTED, cause, lang);
    }
}
