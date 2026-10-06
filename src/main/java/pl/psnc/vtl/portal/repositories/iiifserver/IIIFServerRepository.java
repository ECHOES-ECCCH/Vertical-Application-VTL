package pl.psnc.vtl.portal.repositories.iiifserver;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.controllers.ErrorResponse;
import pl.psnc.vtl.portal.repositories.BackendSystemRepository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import static pl.psnc.vtl.portal.conf.logging.Slf4jMDCFilter.DEFAULT_MDC_UUID_TOKEN_KEY;
import static pl.psnc.vtl.portal.conf.logging.Slf4jMDCFilter.DEFAULT_RESPONSE_TOKEN_HEADER;

@Slf4j
@Repository
public class IIIFServerRepository extends BackendSystemRepository {

    @Value("${vtl.iiif-server-url}")
    private String iiifServerUrl;

    @Value("${vtl.iiif-server-public-url}")
    private String iiifServerPublicUrl;


    public String iiifServerInfoJsonProvider(URI infoJsonUri) {
        HttpRequest request = HttpRequest.newBuilder()
                .setHeader(DEFAULT_RESPONSE_TOKEN_HEADER, MDC.get(DEFAULT_MDC_UUID_TOKEN_KEY))
                .uri(infoJsonUri)
                .GET().build();
        String response = sendAndHandleResponse(request, "getting " + infoJsonUri);

        String old;
        do {
            old = response;
            response = response.replaceAll("(\"http[^\"#]+)#", "$1%23");
        } while (!old.equals(response));

        if (iiifServerUrl.equals(iiifServerPublicUrl)) {
            return response;
        } else {
            return response.replace(iiifServerUrl, iiifServerPublicUrl);
        }
    }


    @Override
    protected void handleException(Exception ex, String actionMessage)
            throws IIIFServerException {
        log.error("Error while " + actionMessage, ex);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(ex.getMessage()).build();
        throw new IIIFServerException("Error while " + actionMessage + "!", errorResponse, ex);
    }

    @Override
    protected void handleErrorResponse(HttpResponse<String> response, String actionMessage) {
        if (response.statusCode() != 200) {
            String errorString = response.body();
            log.error("Error while " + actionMessage + ": " + errorString);
            ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                    .status(response.statusCode()).error(errorString).build();
            throw new IIIFServerException("Error while " + actionMessage + "!", errorResponse);
        }
    }

    @Override
    protected IIIFServerException handleDeserializationException(IOException e, String actionMessage) {
        log.error("Error while parsing response from iiif-server for {}", actionMessage, e);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Error while parsing response from iiif-server for " + actionMessage + ": " + e.getMessage()).build();
        return new IIIFServerException(
                "Error while parsing response from iiif-server for " + actionMessage + "!", errorResponse, e);
    }

    @Override
    protected IIIFServerException handleSerializationException(IOException e, String actionMessage) {
        log.error("Error while serializing request to iiif-server for {}", actionMessage, e);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Error while serializing request to iiif-server for " + actionMessage + ": " + e.getMessage()).build();
        return new IIIFServerException(
                "Error while serializing request to iiif-server for " + actionMessage + "!", errorResponse, e);
    }

}
