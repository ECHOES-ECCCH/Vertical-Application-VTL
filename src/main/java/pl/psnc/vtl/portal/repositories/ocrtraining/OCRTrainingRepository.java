package pl.psnc.vtl.portal.repositories.ocrtraining;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.controllers.ErrorResponse;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.repositories.BackendSystemRepository;
import pl.psnc.vtl.portal.repositories.ocrtools.OcrCorrection;
import pl.psnc.vtl.portal.repositories.ocrtools.OcrCorrectionsBatch;
import pl.psnc.vtl.portal.services.iiifserver.IIIFUriResolver;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static pl.psnc.vtl.portal.conf.logging.Slf4jMDCFilter.DEFAULT_MDC_UUID_TOKEN_KEY;
import static pl.psnc.vtl.portal.conf.logging.Slf4jMDCFilter.DEFAULT_RESPONSE_TOKEN_HEADER;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OCRTrainingRepository extends BackendSystemRepository {
    
    @Value("${vtl.ocr-training-url}")
    private String ocrTrainingUrl;

    private final IIIFUriResolver iiifUriResolver;

    public List<String> getTrainableLanguages() {
        String actionMessage = "getting trainable languages";

        HttpRequest request = HttpRequest.newBuilder()
                .setHeader(DEFAULT_RESPONSE_TOKEN_HEADER, MDC.get(DEFAULT_MDC_UUID_TOKEN_KEY))
                .setHeader("Content-Type", "application/json")
                .uri(URI.create(ocrTrainingUrl + "/api/training/ocr/languages"))
                .GET().build();

        String response = sendAndHandleResponse(request, actionMessage);

        return readResponseValue(response, actionMessage);
    }

    public void sendDataToTrainTesseract(List<Verse> verses, String language) {
        String requestBodyForTesseractTraining = getRequestBodyForTesseractTraining(verses, language);
        if (requestBodyForTesseractTraining.isEmpty()) {
            return;
        }
        HttpRequest request = HttpRequest.newBuilder()
                .setHeader(DEFAULT_RESPONSE_TOKEN_HEADER, MDC.get(DEFAULT_MDC_UUID_TOKEN_KEY))
                .setHeader("Content-Type", "application/json")
                .uri(URI.create(ocrTrainingUrl + "api/training/ocr/corrections"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyForTesseractTraining)).build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("Error while training tesseract, response status code: " + response.statusCode() + " body: "
                        + response.body());
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error while training tesseract: " + e.getMessage());
        }
    }

    private String getRequestBodyForTesseractTraining(List<Verse> verses, String language) {
        verses.forEach(v -> {
            if (v.getLang() == null) {
                v.setLang("pol");
            }
        });

        OcrCorrectionsBatch ocrCorrectionsBatch = new OcrCorrectionsBatch();
        ocrCorrectionsBatch.setCorrections(
                verses.stream()
                        .map(v -> new OcrCorrection(iiifUriResolver.getTrainingImageUrl(v), v.currentContent(),
                                language))
                        .collect(Collectors.toList()));
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ocrCorrectionsBatch);
        } catch (JsonProcessingException e) {
            log.error("Error while parsing body: " + e.getMessage());
        }
        return "";
    }

    @Override
    protected void handleException(Exception ex, String actionMessage)
            throws OCRTrainingException {
        log.error("Error while " + actionMessage, ex);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(ex.getMessage()).build();
        throw new OCRTrainingException("Error while " + actionMessage + "!", errorResponse, ex);
    }

    @Override
    protected void handleErrorResponse(HttpResponse<String> response, String actionMessage)
            throws OCRTrainingException {
        if (response.statusCode() != 200) {
            String errorResponseAsString = response.body();
            log.error("Error while " + actionMessage + ": " + errorResponseAsString);
            try {
                ErrorResponse errorResponse = new ObjectMapper().registerModule(new JavaTimeModule())
                        .readValue(errorResponseAsString, ErrorResponse.class);
                throw new OCRTrainingException("Error while " + actionMessage + "!", errorResponse);
            } catch (IOException e) {
                log.error("Error while parsing error response from ocr-training for " + actionMessage + ": "
                        + errorResponseAsString, e);
                ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("Error while parsing error response from ocr-training for " + actionMessage + ": "
                                + e.getMessage())
                        .build();
                throw new OCRTrainingException(
                        "Error while parsing error response from ocr-training for " + actionMessage + "!",
                        errorResponse, e);
            }
        }

    }

    @Override
    protected OCRTrainingException handleDeserializationException(IOException e, String actionMessage) {
        log.error("Error while parsing response from ocr-training for " + actionMessage, e);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Error while parsing response from ocr-training for " + actionMessage + ": " + e.getMessage())
                .build();
        return new OCRTrainingException(
                "Error while parsing response from ocr-training for " + actionMessage + "!", errorResponse, e);
    }

    @Override
    protected OCRTrainingException handleSerializationException(IOException e, String actionMessage) {
        log.error("Error while serializing request to ocr-training for " + actionMessage, e);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Error while serializing request to ocr-training for " + actionMessage + ": " + e.getMessage())
                .build();
        return new OCRTrainingException(
                "Error while serializing request to ocr-training for " + actionMessage + "!", errorResponse, e);
    }

}
