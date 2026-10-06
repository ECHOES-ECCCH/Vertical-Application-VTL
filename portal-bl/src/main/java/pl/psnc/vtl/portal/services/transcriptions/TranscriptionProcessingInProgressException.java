package pl.psnc.vtl.portal.services.transcriptions;

import pl.psnc.vtl.portal.services.I18nException;

public class TranscriptionProcessingInProgressException extends I18nException {

    public TranscriptionProcessingInProgressException(String message) {
        super(message, "error.error409.operationInProgress");
    }

}
