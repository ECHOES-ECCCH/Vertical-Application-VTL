package pl.psnc.vtl.portal.services.imports;

import org.apache.http.HttpResponse;
import pl.psnc.vtl.portal.controllers.ErrorResponseMessage;
import pl.psnc.vtl.portal.controllers.RequestFieldsValidationException;

import java.net.MalformedURLException;
import java.net.URL;

public class URLValidator {

     public static void validate(String url){
        ErrorResponseMessage message = new ErrorResponseMessage();
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            message.add("resourceUrl", URLErrorMessages.MALFORMED.getErrorMessage());
            throw new RequestFieldsValidationException(message);
        }
    }


    public static RequestFieldsValidationException getNotFoundValidationException() {
        ErrorResponseMessage message = new ErrorResponseMessage();
        message.add("resourceUrl", URLErrorMessages.NOT_FOUND.getErrorMessage());
        return new RequestFieldsValidationException(message);
    }

    public static void validateResponse(HttpResponse response) {
        ErrorResponseMessage message = new ErrorResponseMessage();

        switch (response.getStatusLine().getStatusCode()) {
            case 200:
                return;
            case 404:
                message.add("resourceUrl", URLErrorMessages.NOT_FOUND.getErrorMessage());
                break;
            case 401:
                message.add("resourceUrl", URLErrorMessages.SECURED.getErrorMessage());
                break;
            case 408:
                message.add("resourceUrl", URLErrorMessages.UNRESPONSIVE.getErrorMessage());
                break;
            default:
                message.add("resourceUrl", URLErrorMessages.NOT_PROCESSABLE.getErrorMessage());
                break;
        }
        if (!message.getMessages().isEmpty())
            throw new RequestFieldsValidationException(message);
    }
}
