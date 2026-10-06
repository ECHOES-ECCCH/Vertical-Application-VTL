package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.controllers.transcriptions.TranscriptionGenerationProperties;
import pl.psnc.vtl.portal.dto.transcriptions.PaginatedTranscriptions;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionExportAcceptInfo;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionExportDto;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.transcriptions.TranscriptionExportStatus;
import pl.psnc.vtl.portal.repositories.layers.LayerRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionRepository;
import pl.psnc.vtl.portal.services.eBook.*;
import pl.psnc.vtl.portal.services.imports.BeanLoader;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;

import javax.net.ssl.SSLContext;
import javax.persistence.EntityNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static pl.psnc.vtl.portal.services.registration.LoggedInUserHolder.withLoggedInUser;
import static pl.psnc.vtl.portal.utils.ThreadUtils.withMdc;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TranscriptionExportService {

    private final TranscriptionRepository transcriptionRepository;
    private final TranscriptionPermissionService transcriptionPermissionService;
    private final TranscriptionExportMailService transcriptionExportMailService;
    private final LoggedInUserHolder loggedInUserHolder;
    private final EBookService eBookService;
    private final TranscriptionContentGeneratorService transcriptionContentGeneratorService;
    private final LayerRepository layerRepository;

    @Value("${export.transcription-started-path}")
    private String transcriptionStartedPath;
    @Value("${export.transcription-finished-path}")
    private String transcriptionFinishedPath;
    @Value("${export.trust-self-signed-certificates}")
    private boolean trustSelfSignedCertificates;
    @Value("${export.export-transcription-username}")
    private String exportTranscriptionUsername;
    @Value("${export.export-transcription-password}")
    private String exportTranscriptionPassword;
    @Value("${vtl.iiif-server-url}")
    private String iiifServerUrl;
    @Value("${vtl.iiif-server-public-url}")
    private String iiifServerPublicUrl;

    public void reportTranscriptionStarted(long transcriptionId, String origin) throws Exception {
        if (StringUtils.isBlank(transcriptionStartedPath))
            return;
        Transcription transcription = requireTranscription(transcriptionId);
        HttpPost request = createRequest(transcriptionStartedPath, transcription);
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("externalId", transcription.getExternalId(), ContentType.TEXT_PLAIN);
        builder.addTextBody("transcriptionUrl", origin + "/transcription/" + transcription.getId(),
                ContentType.TEXT_PLAIN);

        final HttpEntity entity = builder.build();
        request.setEntity(entity);
        try (var client = buildClient(); CloseableHttpResponse response = client.execute(request)) {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException(
                        "Unexpected response to transcription started request: " + response.getStatusLine());
            }
            transcriptionRepository.save(transcription);

        } catch (Exception e) {
            BeanLoader.getBean(TranscriptionExportService.class).markExportError(transcription.getId());
            throw e;
        }
    }

    public void markTranscriptionReadyForExport(long transcriptionId) {
        Transcription transcription = requireTranscription(transcriptionId);
        transcriptionPermissionService.checkAdminAccess(transcriptionId);

        if (EnumSet
                .of(TranscriptionExportStatus.NEW, TranscriptionExportStatus.REJECTED, TranscriptionExportStatus.ERROR)
                .contains(transcription.getExportStatus())) {
            transcription.setExportStatus(TranscriptionExportStatus.READY);
            transcriptionRepository.save(transcription);
        }
    }

    public void markTranscriptionAcceptedForExport(long transcriptionId, TranscriptionExportAcceptInfo transcriptionExportAcceptInfo) {
        Transcription transcription = requireTranscription(transcriptionId);
        if (!loggedInUserHolder.isCurrentlyLoggedUserExportReviewerOrAdmin()) {
            throw new AccessDeniedException("User does not have permission for accepting transcription export.");
        }

        if (transcription.getExportStatus() != null && transcription.getExportStatus() == TranscriptionExportStatus.READY) {
            if (transcriptionExportAcceptInfo.getAccepted()) {
                transcription.setExportStatus(TranscriptionExportStatus.ACCEPTED);
                String senderEmail = loggedInUserHolder.getLoggedInUser().getEmail();
                sendTranscriptionExportRequest(transcription, senderEmail);
            } else {
                transcription.setExportStatus(TranscriptionExportStatus.REJECTED);
                transcriptionExportMailService.sendEmail(transcription, transcriptionExportAcceptInfo.getMessage(),
                        transcription.getCreator().getEmail());

            }
            transcriptionRepository.save(transcription);
        }
    }

    private void sendTranscriptionExportRequest(Transcription transcription, String senderEmail) {
        CompletableFuture.runAsync(withMdc(withLoggedInUser(() -> {
            try {
                BeanLoader.getBean(TranscriptionExportService.class)
                        .doSendTranscriptionExportRequest(transcription, senderEmail);
            } catch (Exception e) {
                log.error("Error exporting transcription " + transcription.getId(), e);
                BeanLoader.getBean(TranscriptionExportService.class).markExportError(transcription.getId());
            }
        })));
    }

    public void doSendTranscriptionExportRequest(Transcription transcription, String senderEmail) throws IOException {
            Layer firstLayer = layerRepository.findFirstByTranscriptionIdOrderById(transcription.getId());
            EbookDto ebookDto = new EbookDto(Ebook.PDF, transcription.getId(), firstLayer.getId(), false, null);
            String ebookId = eBookService.addExportProcess(ebookDto);

            String ebookURL = null;
            AsyncTaskResponse ebookResponse;
            do {
                ebookResponse = eBookService.getEBookLocation(ebookId);
                if (ebookResponse.getStatus() == Status.READY) {
                    ebookURL = ebookResponse.getDetails();
                    ebookURL = ebookURL.replace(iiifServerPublicUrl, iiifServerUrl);
                } else {
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                }
            } while (ebookURL == null && ebookResponse.getStatus() == Status.PROCESSING);

            if (ebookURL == null) {
                throw new RuntimeException("Cannot create pdf file for transcription " + transcription.getId());
            }

            byte[] ebookBytes;
            try (var client = buildClient(); var response = client.execute(new HttpGet(ebookURL))) {
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new RuntimeException(
                            "Unexpected response to PDF content request: " + response.getStatusLine());
                }
                try (InputStream inputStream = response.getEntity().getContent()) {
                    ebookBytes = IOUtils.toByteArray(inputStream);
                }
            }
            log.debug("PDF prepared for export for transcription {}, size: {}",
                    transcription.getId(), ebookBytes.length);

            var altoContents = transcriptionContentGeneratorService.prepareLayerContents(
                    TranscriptionGenerationProperties.builder()
                            .contentType(TranscriptionPageContentType.ALTO)
                            .transcriptionId(transcription.getId())
                            .layerId(firstLayer.getId()).build());
            log.debug("ALTO prepared for export for transcription {}", transcription.getId());

            HttpPost request = createRequest(transcriptionFinishedPath, transcription);

            final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            ContentType ct = ContentType.create("text/plain", StandardCharsets.UTF_8);
            builder.addTextBody("externalId", transcription.getExternalId(), ct);
            builder.addTextBody("user", senderEmail, ct);
            ct = ContentType.create("text/xml", StandardCharsets.UTF_8);
            for (int i = 0; i < altoContents.size(); i++) {
                var pair = altoContents.get(i);
                builder.addTextBody(String.format("alto%04d", i + 1), pair.getRight(), ct);
            }
            builder.addBinaryBody("pdf", ebookBytes);

            final HttpEntity entity = builder.build();
            request.setEntity(entity);

            log.info("Sending export request for transcription " + transcription.getId());

            try (var client = buildClient(); var response = client.execute(request)) {
                Transcription transcription2 = requireTranscription(transcription.getId()); // make sure it's up to date
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    transcription2.setExportStatus(TranscriptionExportStatus.SENT);
                    EntityUtils.consumeQuietly(response.getEntity());
                } else {
                    log.error("Export request failed for transcription " + transcription2.getId() + " with status "
                            + response.getStatusLine());
                    log.debug("Response: {}", EntityUtils.toString(response.getEntity()));
                    transcription2.setExportStatus(TranscriptionExportStatus.ERROR);
                }
                transcriptionRepository.save(transcription2);
            }
    }

    private CloseableHttpClient buildClient() {
        final RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(5000).build();
        HttpClientBuilder builder = HttpClientBuilder.create().setDefaultRequestConfig(config);
        if (trustSelfSignedCertificates) {
            try {
                SSLContext sslContext =
                        SSLContextBuilder.create().loadTrustMaterial(new TrustSelfSignedStrategy()).build();
                builder.setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier()));
            } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
                throw new RuntimeException(e);
            }
        }
        return builder.build();
    }

    private HttpPost createRequest(String path, Transcription transcription) {
        URI uri = null;
        try {
            uri = new URI(transcription.getExternalId());
        } catch (URISyntaxException e) {
            // try externalUrl
        }
        if (uri == null || !"https".equals(uri.getScheme())) {
            uri = URI.create(transcription.getExternalUrl());
        }

        String authString = String.format("%s:%s", exportTranscriptionUsername, exportTranscriptionPassword);
        String basicAuthPayload = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes());

        HttpPost request = new HttpPost(uri.resolve(path));
        request.setHeader(HttpHeaders.AUTHORIZATION, basicAuthPayload);
        return request;
    }

    public void markExportError(long transcriptionId) {
        Transcription transcription = requireTranscription(transcriptionId);
        transcription.setExportStatus(TranscriptionExportStatus.ERROR);
        transcriptionRepository.save(transcription);
    }

    public void resetTranscriptionExportStatus(long transcriptionId) {
        Transcription transcription = requireTranscription(transcriptionId);

        if (transcription.getExportStatus() != null && transcription.getExportStatus() != TranscriptionExportStatus.UNDEFINED && transcription.getExportStatus() != TranscriptionExportStatus.NEW) {
            transcription.setExportStatus(TranscriptionExportStatus.NEW);
            transcriptionRepository.save(transcription);
        }
    }

    public PaginatedTranscriptions<TranscriptionExportDto> getTranscriptionsReadyForExport(int page, int perpage, Sort.Direction orderDir) {
        if (!loggedInUserHolder.isCurrentlyLoggedUserExportReviewerOrAdmin()) {
            throw new AccessDeniedException("User does not have permission for accepting transcription export.");
        }

        PageRequest pageable = PageRequest.of(page - 1, perpage, Sort.by(orderDir, "lastModificationDate"));
        Page<TranscriptionExportDto> data = transcriptionRepository.findTranscriptionExportByStatuses(List.of(TranscriptionExportStatus.READY), pageable);

        return PaginatedTranscriptions.<TranscriptionExportDto>builder().page(page).perpage(perpage).hits(data.getTotalElements())
                .count(data.getNumberOfElements()).pages(totalPages(data.getTotalElements(), perpage))
                .transcriptions(data.getContent()).build();
    }

    private Transcription requireTranscription(Long transcriptionId) {
        return transcriptionRepository.findById(transcriptionId).orElseThrow(() -> new EntityNotFoundException(
                "Unable to find " + Transcription.class.getName() + " with id '" + transcriptionId + "'!"));
    }

    private int totalPages(long totalElements, int perpage) {
        int pages = (int) (totalElements / perpage);
        if (totalElements % perpage > 0) {
            pages++;
        }
        return pages;
    }
}
