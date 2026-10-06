package pl.psnc.vtl.ocrtraining.domain.iiif.exception;

import pl.psnc.vtl.ocrtraining.domain.common.exception.ErrorCode;
import pl.psnc.vtl.ocrtraining.domain.common.exception.VtlException;


public class IIIFImageDownloadException extends VtlException {

    public IIIFImageDownloadException(Throwable cause) {
        super(ErrorCode.IIIF_IMAGE_DOWNLOAD_FAILED, cause);
    }
}
