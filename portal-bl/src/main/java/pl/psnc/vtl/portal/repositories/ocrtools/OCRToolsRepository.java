package pl.psnc.vtl.portal.repositories.ocrtools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.controllers.ErrorResponse;
import pl.psnc.vtl.portal.repositories.BackendSystemException;
import pl.psnc.vtl.portal.repositories.BackendSystemRepository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static pl.psnc.vtl.portal.conf.logging.Slf4jMDCFilter.DEFAULT_MDC_UUID_TOKEN_KEY;
import static pl.psnc.vtl.portal.conf.logging.Slf4jMDCFilter.DEFAULT_RESPONSE_TOKEN_HEADER;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OCRToolsRepository extends BackendSystemRepository {

    @Value("${vtl.ocr-tools-url}")
    private String ocrToolsUrl;

    private static final int MAX_RETRY_COUNT = 3;

    public String recognizePage(Long transcriptionId, String filename, Integer page, String lang, boolean handleColumns)
            throws OCRToolsException {
        String uri = ocrToolsUrl + "api/ocr/" + transcriptionId + "/recognize-hocr/" + encode(filename, UTF_8)
                + "?lang=" + encode(lang, UTF_8)
                + "&handleColumns=" + handleColumns;
        if (page != null)
            uri += "&page=" + page;

        HttpRequest request = HttpRequest.newBuilder()
                .setHeader(DEFAULT_RESPONSE_TOKEN_HEADER, MDC.get(DEFAULT_MDC_UUID_TOKEN_KEY))
                .uri(URI.create(uri))
                .GET().build();
        return sendAndHandleResponse(request,
                "recognizing page " + filename + " for transcription '" + transcriptionId + "'");
    }


    @Override
    protected String sendAndHandleResponse(HttpRequest request, String actionMessage)
            throws BackendSystemException {
        int retry = MAX_RETRY_COUNT;
        HttpResponse<String> response = null;
        while (retry > 0) {
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                break;
            } catch (IOException | InterruptedException e) {
                retry--;
                if (retry == 0) {
                    handleException(e, actionMessage);
                }
            }
        }
        handleErrorResponse(response, actionMessage);
        return response.body();
    }

    public Map<Long, String> recognizePageFragments(Long transcriptionId, String filename, Integer page,
            List<OcrBoundingBox> boxes, String lang, boolean hocr, boolean singleWordMode)
            throws OCRToolsException {
        String actionMessage = "recognizing verses of " + filename + " for transcription '" + transcriptionId + "'";
        String requestBody = writeRequestValue(new OcrBatchRequest(boxes), actionMessage);

        String uri = ocrToolsUrl + "api/ocr/" + transcriptionId + "/recognize-subimg-batch/"
                + encode(filename, UTF_8)
                + "?lang=" + encode(lang, UTF_8)
				+ "&hocr=" + hocr
                + "&singleWord=" + singleWordMode;
        if (page != null)
            uri += "&page=" + page;

        HttpRequest request = HttpRequest.newBuilder()
                .setHeader(DEFAULT_RESPONSE_TOKEN_HEADER, MDC.get(DEFAULT_MDC_UUID_TOKEN_KEY))
                .setHeader("Content-Type", "application/json")
                .uri(URI.create(uri))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();

        String response = sendAndHandleResponse(request, actionMessage);

        Map<String, Map<String, String>> map = readResponseValue(response, actionMessage);
        return map.get("versesContent").entrySet().stream()
                .collect(Collectors.toMap(e -> Long.valueOf(e.getKey()), Map.Entry::getValue));
    }

    public List<String> getTrainedLanguages() {
        String actionMessage = "getting trained languages";

        HttpRequest request = HttpRequest.newBuilder()
                .setHeader(DEFAULT_RESPONSE_TOKEN_HEADER, MDC.get(DEFAULT_MDC_UUID_TOKEN_KEY))
                .setHeader("Content-Type", "application/json")
                .uri(URI.create(ocrToolsUrl + "/api/ocr/languages"))
                .GET().build();

        String response = sendAndHandleResponse(request, actionMessage);

        return readResponseValue(response, actionMessage);
    }

    @Override
    protected void handleException(Exception ex, String actionMessage)
            throws OCRToolsException {
        log.error("Error while " + actionMessage, ex);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(ex.getMessage()).build();
        throw new OCRToolsException("Error while " + actionMessage + "!", errorResponse, ex);
    }

    @Override
    protected void handleErrorResponse(HttpResponse<String> response, String actionMessage)
            throws OCRToolsException {
        if (response.statusCode() != 200) {
            String errorResponseAsString = response.body();
            log.error("Error while " + actionMessage + ": " + errorResponseAsString);
            try {
                ErrorResponse errorResponse = new ObjectMapper().registerModule(new JavaTimeModule())
                        .readValue(errorResponseAsString, ErrorResponse.class);
                throw new OCRToolsException("Error while " + actionMessage + "!", errorResponse);
            } catch (IOException e) {
                log.error("Error while parsing error response from ocr-tools for " + actionMessage + ": " + errorResponseAsString, e);
                ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("Error while parsing error response from ocr-tools for " + actionMessage + ": " + e.getMessage()).build();
                throw new OCRToolsException(
                        "Error while parsing error response from ocr-tools for " + actionMessage + "!", errorResponse, e);
            }
        }

    }

    @Override
    protected OCRToolsException handleDeserializationException(IOException e, String actionMessage) {
        log.error("Error while parsing response from ocr-tools for " + actionMessage, e);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Error while parsing response from ocr-tools for " + actionMessage + ": " + e.getMessage()).build();
        return new OCRToolsException(
                "Error while parsing response from ocr-tools for " + actionMessage + "!", errorResponse, e);
    }

    @Override
    protected OCRToolsException handleSerializationException(IOException e, String actionMessage) {
        log.error("Error while serializing request to ocr-tools for " + actionMessage, e);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Error while serializing request to ocr-tools for " + actionMessage + ": " + e.getMessage()).build();
        return new OCRToolsException(
                "Error while serializing request to ocr-tools for " + actionMessage + "!", errorResponse, e);
    }
}
