package pl.psnc.vtl.ocrtraining.domain.iiif.exception;

import pl.psnc.vtl.ocrtraining.domain.common.exception.ErrorCode;
import pl.psnc.vtl.ocrtraining.domain.common.exception.VtlException;


public class InvalidIIIFUrlException extends VtlException {

    public InvalidIIIFUrlException(String iiifUrl) {
        super(ErrorCode.IIIF_MALFORMED_URL, iiifUrl);
    }

    public InvalidIIIFUrlException(String iiifUrl, Throwable cause) {
        super(ErrorCode.IIIF_MALFORMED_URL, cause, iiifUrl);
    }
}
