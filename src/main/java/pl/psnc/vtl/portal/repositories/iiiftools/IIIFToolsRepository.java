package pl.psnc.vtl.portal.repositories.iiiftools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.apache.http.Consts;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import pl.psnc.vtl.portal.controllers.ErrorResponse;
import pl.psnc.vtl.portal.dto.eBook.PdfTitlePage;
import pl.psnc.vtl.portal.repositories.BackendSystemRepository;
import pl.psnc.vtl.portal.services.eBook.ConversionException;
import pl.psnc.vtl.portal.services.eBook.EBookStorageAware;
import pl.psnc.vtl.portal.services.eBook.Ebook;
import pl.psnc.vtl.portal.services.eBook.EbookDto;
import pl.psnc.vtl.portal.services.pages.WrongFileTypeException;
import pl.psnc.vtl.portal.services.pages.zip.NonClosableInputStreamPublisher;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.IntConsumer;

import static pl.psnc.vtl.portal.conf.logging.Slf4jMDCFilter.DEFAULT_MDC_UUID_TOKEN_KEY;
import static pl.psnc.vtl.portal.conf.logging.Slf4jMDCFilter.DEFAULT_RESPONSE_TOKEN_HEADER;

@Slf4j
@Repository
public class IIIFToolsRepository extends BackendSystemRepository implements EBookStorageAware {

    //if we do it this way we have to add "api/iiif/" in every request
    @Value("${vtl.iiif-tools-url}")
    private String iiifToolsUrl;

    @Value("${vtl.iiif-server-url}")
    private String iiifServerUrl;

    @Value("${vtl.iiif-server-public-url}")
    private String iiifServerPublicUrl;

    private RestTemplate restTemplate = new RestTemplate();

    private static final ContentType TEXT_PLAIN_LATAIN = ContentType.create("text/plain", Consts.UTF_8);


    public void savePageImage(Long transcriptionId, InputStream imageInputStream, String filename)
            throws IIIFToolsException {
        filename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        log.debug("Saving page " + filename + " of transcription " + transcriptionId);
        HttpRequest request = HttpRequest.newBuilder()
                .setHeader(DEFAULT_RESPONSE_TOKEN_HEADER, MDC.get(DEFAULT_MDC_UUID_TOKEN_KEY))
                .setHeader("Content-Type", "application/octet-stream")
                .uri(URI.create(iiifToolsUrl + "api/iiif/" + transcriptionId + "/save-image/" + filename))
                .POST(new NonClosableInputStreamPublisher(() -> imageInputStream)).build();

        sendAndHandleResponse(request, "saving image " + filename + " for transcription '" + transcriptionId + "'");
    }

    @Transactional(propagation = Propagation.NEVER)
    public Map<String, List<String>> preparePyramids(long transcriptionId, Collection<String> fileNames,
            IntConsumer progressMonitor) {
        Map<String, List<String>> result = new HashMap<>();
        int count = 0;
        for (String file : fileNames) {
            result.put(file, makePagePyramid(transcriptionId, file));
            count++;
            if (progressMonitor != null)
                progressMonitor.accept(count);
        }

        Map<String, Integer> filesPagesCounts = getFilesPages(transcriptionId, new ArrayList<>(fileNames));
        result.forEach((name, pyramids) -> {
            if (filesPagesCounts.get(name) != pyramids.size()) {
                throw new WrongFileTypeException(
                        "Provided file '" + name + "' for transcription " + transcriptionId +
                                " cannot be properly converted to pyramidal files!",
                        "wrongConversion",
                        name);
            }
        });
        return result;
    }

    public List<String> makePagePyramid(Long transcriptionId, String filename)
            throws IIIFToolsException {
        filename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        log.debug("Making page " + filename + " of transcription " + transcriptionId + " pyramidal tiff");

        HttpRequest request = HttpRequest.newBuilder()
                .setHeader(DEFAULT_RESPONSE_TOKEN_HEADER, MDC.get(DEFAULT_MDC_UUID_TOKEN_KEY))
                .uri(URI.create(iiifToolsUrl + "api/iiif/" + transcriptionId + "/make-pyramid/" + filename))
                .PUT(HttpRequest.BodyPublishers.noBody()).build();
        String actionMessage = "making pyramidal tiff for " + filename + " for transcription '" + transcriptionId + "'";
        String response = sendAndHandleResponse(request, actionMessage);

        return readResponseValue(response, actionMessage);
    }

    public void saveEbook(long transcriptionId, String hash, List<MultipartFile> files, String title, String author,
                            Ebook format, HashMap<String, String> pagesIiifUrls, PdfTitlePage pdfTitlePage)
            throws IOException {
        HttpPost post = new HttpPost(URI.create(iiifToolsUrl + "api/transcriptions/" + transcriptionId + "/eBooks"));

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.setCharset(StandardCharsets.UTF_8);
        for (MultipartFile f : files) {
            builder.addBinaryBody("files", f.getBytes(), ContentType.DEFAULT_BINARY, f.getOriginalFilename());
        }
        if (pdfTitlePage != null) {
            builder.addTextBody("titlepage", new ObjectMapper().writeValueAsString(pdfTitlePage), ContentType.APPLICATION_JSON);
        }
        builder.addTextBody("title", title, TEXT_PLAIN_LATAIN);
        builder.addTextBody("author", author, TEXT_PLAIN_LATAIN);
        builder.addTextBody("format", format.toString().toUpperCase(), TEXT_PLAIN_LATAIN);
        builder.addTextBody("hash", hash, TEXT_PLAIN_LATAIN);
        JSONObject iiifUrlsMap = new JSONObject(pagesIiifUrls);
        builder.addTextBody("mapping", iiifUrlsMap.toJSONString());

        org.apache.http.HttpEntity entity = builder.build();
        post.setEntity(entity);
        try (CloseableHttpClient client = HttpClients.createDefault();
                CloseableHttpResponse response = client.execute(post);) {
            if (response.getStatusLine().getStatusCode() != 201) {
                throw new ConversionException(hash);
            }
        }

    }

    public void clearIiifServer()
            throws IIIFToolsException {
        log.debug("Clearing IIIF server resources");
        HttpRequest request = HttpRequest.newBuilder()
                .setHeader(DEFAULT_RESPONSE_TOKEN_HEADER, MDC.get(DEFAULT_MDC_UUID_TOKEN_KEY))
                .uri(URI.create(iiifToolsUrl + "api/iiif/clear-all"))
                .DELETE().build();

        sendAndHandleResponse(request, "clearing IIIF server resources");
    }


    public RemoveFileResponse deleteTranscriptionImages(long transcriptionId)
            throws IIIFToolsException {
        HttpRequest request = HttpRequest.newBuilder()
                .setHeader(DEFAULT_RESPONSE_TOKEN_HEADER, MDC.get(DEFAULT_MDC_UUID_TOKEN_KEY))
                .uri(URI.create(iiifToolsUrl + "api/iiif/" + transcriptionId))
                .DELETE().build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), RemoveFileResponse.class);
        } catch (HttpStatusCodeException | IOException | InterruptedException e) {
            throw new FailedToSavePagesException("Deleting images failed for transcription " + transcriptionId);
        }
    }


    public RemoveFileResponse deletePageImage(long transcriptionId, String filename) {
        HttpRequest request = HttpRequest.newBuilder()
                .setHeader(DEFAULT_RESPONSE_TOKEN_HEADER, MDC.get(DEFAULT_MDC_UUID_TOKEN_KEY))
                .uri(URI.create(iiifToolsUrl + "api/iiif/" + transcriptionId + "/delete-image/" + filename))
                .DELETE().build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), RemoveFileResponse.class);
        } catch (HttpStatusCodeException | IOException | InterruptedException e) {
            throw new FailedToSavePagesException("Deleting page image failed " + filename);
        }
    }

    public void deletePagePyramid(long transcriptionId, String filename) {
        HttpRequest request = HttpRequest.newBuilder()
                .setHeader(DEFAULT_RESPONSE_TOKEN_HEADER, MDC.get(DEFAULT_MDC_UUID_TOKEN_KEY))
                .uri(URI.create(iiifToolsUrl + "api/iiif/" + transcriptionId + "/delete-pyramid/" + filename))
                .DELETE().build();

        sendAndHandleResponse(request, "delete page pyramid" + filename);
    }

    public Map<String, Integer> getFilesPages(long transcriptionId, List<String> filesNames)
            throws IIIFToolsException {
        String actionMessage =
                "getting number of pages for " + filesNames + " for transcription '" + transcriptionId + "'";
        String requestBody = writeRequestValue(filesNames, actionMessage);

        HttpRequest request = HttpRequest.newBuilder()
                .setHeader(DEFAULT_RESPONSE_TOKEN_HEADER, MDC.get(DEFAULT_MDC_UUID_TOKEN_KEY))
                .setHeader("Content-Type", "application/json")
                .uri(URI.create(iiifToolsUrl + "api/iiif/" + transcriptionId + "/pages"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();

        String response = sendAndHandleResponse(request, actionMessage);

        return readResponseValue(response, actionMessage);
    }

    @Override
    protected void handleException(Exception ex, String actionMessage)
            throws IIIFToolsException {
        log.error("Error while " + actionMessage, ex);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(ex.getMessage()).build();
        throw new IIIFToolsException("Error while " + actionMessage + "!", errorResponse, ex);
    }


    @Override
    protected void handleErrorResponse(HttpResponse<String> response, String actionMessage)
            throws IIIFToolsException {
        if (response.statusCode() != 200) {
            String errorResponseAsString = response.body();
            String errorMessage = "Error while parsing error response from iiif-tools for ";
            log.error("Error while " + actionMessage + ": " + errorResponseAsString);
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.findAndRegisterModules();
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                ErrorResponse errorResponse = objectMapper.readValue(errorResponseAsString, ErrorResponse.class);
                throw new IIIFToolsException("Error while " + actionMessage + "!", errorResponse);
            } catch (IOException e) {
                log.error(errorMessage + actionMessage + ": " + errorResponseAsString, e);
                ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error(errorMessage + actionMessage + ": " + e.getMessage()).build();
                throw new IIIFToolsException(errorMessage + actionMessage + "!", errorResponse, e);
            }
        }
    }


    @Override
    protected IIIFToolsException handleDeserializationException(IOException e, String actionMessage) {
        String message = "Error while parsing response from iiif-tools for ";
        log.error(message + actionMessage, e);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(message + actionMessage + ": " + e.getMessage())
                .build();
        return new IIIFToolsException(message + actionMessage + "!", errorResponse, e);
    }


    @Override
    protected IIIFToolsException handleSerializationException(IOException e, String actionMessage) {
        String errorMessage = "Error while serializing request to iiif-tools for ";
        log.error(errorMessage + actionMessage, e);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(errorMessage + actionMessage + ": " + e.getMessage()).build();
        return new IIIFToolsException(errorMessage + actionMessage + "!", errorResponse, e);
    }


    public DownloadFilesResponse saveTranscriptionFiles(Long transcriptionId, List<String> links,
            IntConsumer progressMonitor) {
        String requestUri = iiifToolsUrl + "api/iiif/" + transcriptionId + "/download-images";
        var typeRef = new ParameterizedTypeReference<DownloadFilesResponse>() {
            /* empty */ };

        Map<String, String> names = new HashMap<>();
        long quota = 0;
        try {
            for (int i = 0; i < links.size(); i++) {
                HttpEntity<List<String>> requestBody = new HttpEntity<>(links.subList(i, i + 1), null);
                ResponseEntity<DownloadFilesResponse> res =
                        restTemplate.exchange(requestUri, HttpMethod.POST, requestBody, typeRef);
                DownloadFilesResponse body = res.getBody();
                names.putAll(body.getUrlsToFilenames());
                quota += body.getQuota();

                if (progressMonitor != null)
                    progressMonitor.accept(i + 1);
            }
            return new DownloadFilesResponse(names, quota);
        } catch (HttpStatusCodeException e) {
            throw new FailedToSavePagesException("Failed to save pages in IIIF Tools");
        }
    }


    public void deletePageData(long transcriptionId, String path, int relativeNo, boolean sharedPage) {
        DeleteDataDto deleteDataDto = new DeleteDataDto(relativeNo, path, sharedPage);
        HttpRequest request = HttpRequest.newBuilder()
                .setHeader(DEFAULT_RESPONSE_TOKEN_HEADER, MDC.get(DEFAULT_MDC_UUID_TOKEN_KEY))
                .setHeader("Content-Type", "application/json")
                .uri(URI.create(iiifToolsUrl + "api/iiif/" + transcriptionId + "/delete-page"))
                .POST(HttpRequest.BodyPublishers.ofString(writeRequestValue(deleteDataDto, "delete page" + path + "data"))).build();

        sendAndHandleResponse(request, "delete page" + path + "data");
    }

    @Override
    public ExportedEBook getEBookLocation(String hashCode, EbookDto ebookDto) {
        URI query = UriComponentsBuilder
                .fromHttpUrl(String.format("%sapi/transcriptions/%d/eBooks", iiifToolsUrl, ebookDto.getTranscriptionId()))
                .queryParam("format", ebookDto.getFileFormat().name())
                .queryParam("hash", hashCode).build()
                .toUri();

        return restTemplate.getForObject(query, ExportedEBook.class);
    }


    @Data
    @AllArgsConstructor
    private final class DeleteDataDto {

        int relativeNo;
        String path;
        boolean sharedPage;
    }
}

