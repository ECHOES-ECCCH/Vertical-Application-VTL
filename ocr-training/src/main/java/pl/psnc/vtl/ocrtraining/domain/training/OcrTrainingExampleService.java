package pl.psnc.vtl.ocrtraining.domain.training;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import pl.psnc.vtl.ocrtraining.domain.common.file.FileSystemRepository;
import pl.psnc.vtl.ocrtraining.domain.common.image.ImageMediaType;
import pl.psnc.vtl.ocrtraining.domain.common.image.ImageUtils;
import pl.psnc.vtl.ocrtraining.domain.iiif.exception.IIIFImageDownloadException;
import pl.psnc.vtl.ocrtraining.domain.training.dto.OcrCorrection;
import pl.psnc.vtl.ocrtraining.domain.training.dto.OcrCorrectionsBatch;
import pl.psnc.vtl.ocrtraining.domain.training.exception.InvalidOcrCorrectionsNumberException;
import pl.psnc.vtl.ocrtraining.domain.training.exception.UnsupportedOcrLanguageException;

import javax.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Slf4j
public class OcrTrainingExampleService {

    private static final int MAX_OCR_CORRECTIONS_NUM = 100_000;

    private final ApplicationContext applicationContext;
    private final OcrTrainingService ocrTrainingService;
    private final OcrTrainingExampleRepository ocrTrainingExampleRepository;
    private final FileSystemRepository fileSystemRepository;
    private final PendingOcrCorrectionRepository pendingOcrCorrectionRepository;

    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${vtl.iiif-server.request-timeout-ms}")
    private long requestTimeoutMillis;
    @Value("${vtl.ocr.corrections.queue.interval-minutes}")
    private int fetchIntervalMinutes;

    private RestTemplate restTemplate;

    @PostConstruct
    private void init() {
        restTemplate = restTemplateBuilder
                .setReadTimeout(Duration.ofMillis(requestTimeoutMillis))
                .build();
    }

    public void validateOcrCorrections(OcrCorrectionsBatch ocrCorrections)
            throws InvalidOcrCorrectionsNumberException, UnsupportedOcrLanguageException {
    
        List<OcrCorrection> corrections = ocrCorrections.getCorrections();
        if (corrections == null || corrections.isEmpty() || corrections.size() > MAX_OCR_CORRECTIONS_NUM)
            throw new InvalidOcrCorrectionsNumberException();

        List<String> langs = ocrTrainingService.getTrainableLanguages();
    
        List<String> unsupportedLangs = corrections.stream()
                .map(OcrCorrection::getLang)
                .distinct()
                .filter(lang -> !langs.contains(lang))
                .collect(toList());
    
        if (!unsupportedLangs.isEmpty())
            throw new UnsupportedOcrLanguageException(unsupportedLangs);
    }

    @Transactional
    public void savePendingTrainingExample(OcrCorrectionsBatch ocrCorrections) {
        pendingOcrCorrectionRepository.saveAll(ocrCorrections.getCorrections().stream()
                .map(correction -> new PendingOcrCorrection(correction.getIiifImageUrl(), ImageMediaType.JPEG,
                        correction.getCorrectText(), correction.getLang()))
                .collect(toList()));
    }

    @Transactional(readOnly = true)
    public void processPendingTrainingExamples() {
        LocalDateTime lastRetryBefore = LocalDateTime.now().minusMinutes(fetchIntervalMinutes);
        Stream<PendingOcrCorrection> examples =
                pendingOcrCorrectionRepository.findByLastRetryBeforeOrLastRetryIsNullOrderByQueueTime(lastRetryBefore);
        OcrTrainingExampleService self = applicationContext.getBean(OcrTrainingExampleService.class);
        examples.forEach(data -> {
            try {
                log.info("Processing pending training example {} for lang {}, ulr: {}", data.getId(), data.getLang(),
                        data.getIiifImageUrl());
                self.storeOcrTrainingExample(data);
            } catch (Exception e) {
                log.error("Pending training example {} processing failed", data.getId(), e);
                self.saveForRetry(data);
            }
        });
    }

    /**
     * Persists the training example (image-text pair) for further training purposes
     * @param data dto containing ocr image and correct text which the image
     *             presents
     * @return details object containing data about the newly created training
     *         example
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
    public OcrTrainingExample storeOcrTrainingExample(PendingOcrCorrection data)
            throws IOException, IIIFImageDownloadException {

        FileSystemTransaction fsTransaction = new FileSystemTransaction(fileSystemRepository);
        attachTransactionSynchronization(fsTransaction);

        OcrTrainingExample trainingExample = ocrTrainingExampleRepository
                .findByIiifImgUrlAndLang(data.getIiifImageUrl(), data.getLang()).orElse(null);
        if (trainingExample == null) {
            String groundTruthName = UUID.randomUUID().toString();
            Path groundTruthFolder = ocrTrainingService.toGroundTruthPath(data.getLang());
            Path imagePath = groundTruthFolder.resolve(groundTruthName + ".png");
            Path ocrTextPath = groundTruthFolder.resolve(groundTruthName + ".gt.txt");
            trainingExample = new OcrTrainingExample(ocrTextPath, data.getIiifImageUrl(), data.getLang(),
                    imagePath, ImageMediaType.PNG);
            trainingExample = ocrTrainingExampleRepository.save(trainingExample);
            ocrTrainingService.incrementLangStatsNewExamplesNum(data.getLang(), 1);
        }

        if (fileSystemRepository.fileExists(trainingExample.getOcrTextPath())) {
            fsTransaction.updateFile(data.getCorrectText().getBytes(UTF_8), trainingExample.getOcrTextPath());
        } else {
            fsTransaction.saveNewFile(data.getCorrectText().getBytes(UTF_8), trainingExample.getOcrTextPath());
        }

        if (fileSystemRepository.fileExists(trainingExample.getImagePath())) {
            // assuming image has not changed (url is the same)
        } else {
            ImageMediaType format = data.getImageFormat();
            byte[] imageContent = downloadIIIFImage(data.getIiifImageUrl(), format);
            byte[] imageContentPng = format == ImageMediaType.PNG ? imageContent
                    : ImageUtils.convertImage(imageContent, ImageMediaType.PNG);
            fsTransaction.saveNewFile(imageContentPng, trainingExample.getImagePath());
        }

        pendingOcrCorrectionRepository.deleteById(data.getId());
        return trainingExample;
    }

    private void attachTransactionSynchronization(FileSystemTransaction transaction) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status == TransactionSynchronization.STATUS_COMMITTED)
                            transaction.commit();
                        else
                            transaction.rollback();

                        super.afterCompletion(status);
                    }
                });
    }

    private byte[] downloadIIIFImage(String iiifImgUrl, ImageMediaType imgFormat) throws IIIFImageDownloadException {
        MediaType mediaType = ImageUtils.resolveMediaType(imgFormat);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(mediaType));

        HttpHeaders headers = httpHeaders;
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(iiifImgUrl, HttpMethod.GET, entity, byte[].class);
            return response.getBody();
        } catch (RestClientException e) {
            throw new IIIFImageDownloadException(e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveForRetry(PendingOcrCorrection data) {
        PendingOcrCorrection toRetry = new PendingOcrCorrection(data, LocalDateTime.now());
        pendingOcrCorrectionRepository.deleteById(data.getId());
        pendingOcrCorrectionRepository.save(toRetry);
    }

}
