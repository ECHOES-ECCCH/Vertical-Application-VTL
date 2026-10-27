package pl.psnc.vtl.portal.services.imports;

import pl.psnc.vtl.portal.controllers.ErrorResponseMessage;
import pl.psnc.vtl.portal.controllers.RequestFieldsValidationException;

public class TextFormatValidator {
    public static RequestFieldsValidationException getMalformedXMLException() {
        ErrorResponseMessage message = new ErrorResponseMessage();
        message.add("resourceUrl", TextFormatErrorMessages.MALFORMED.getErrorMessage());
        return new RequestFieldsValidationException(message);
    }

    public static RequestFieldsValidationException getWrongTypeXMLException() {
        ErrorResponseMessage message = new ErrorResponseMessage();
        message.add("resourceUrl", TextFormatErrorMessages.WRONG_TYPE.getErrorMessage());
        return new RequestFieldsValidationException(message);
    }

    public static RequestFieldsValidationException getUnsupportedTypesException() {
        ErrorResponseMessage message = new ErrorResponseMessage();
        message.add("resourceUrl", TextFormatErrorMessages.UNSUPPORTED_TYPES.getErrorMessage());
        return new RequestFieldsValidationException(message);
    }

    public static RequestFieldsValidationException linkLessPublicationException() {
        ErrorResponseMessage message = new ErrorResponseMessage();
        message.add("resourceUrl", TextFormatErrorMessages.LINK_LESS.getErrorMessage());
        return new RequestFieldsValidationException(message);
    }
}
