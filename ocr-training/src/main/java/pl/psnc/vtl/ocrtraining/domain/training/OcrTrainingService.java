package pl.psnc.vtl.ocrtraining.domain.training;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.ocrtraining.domain.training.exception.OcrTrainingAlreadyInProgressException;

import javax.persistence.OptimisticLockException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OcrTrainingService {
    private static final int TRAINING_MIN_NEW_EXAMPLES = 100;

    @Value("${vtl.ocr.training.tessdata-dir}")
    private String tessdataDir;
    @Value("${vtl.ocr.training.langdata-dir}")
    private String langdataDir;
    @Value("${vtl.ocr.training.trained-langs-dir}")
    private String trainedLangsDir;

    private final OcrTrainingLangStatsRepository ocrTrainingLangStatsRepository;

    @Value("${vtl.ocr.training.procedure.max-iterations}")
    private int maxTrainingIterations;

    private final Lock trainingProcedureLock = new ReentrantLock();

    @Transactional(readOnly = true)
    public List<String> getTrainableLanguages() {
        return ocrTrainingLangStatsRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(tl -> tl.getLang()).collect(toList());
    }

    /**
     * Resolves the next training procedure and performs it
     * The training procedure resolution is based on ocr language and the number of examples containing text in specified language
     */
    public void resolveAndStartTrainingProcedure() {
        log.info("Executing OCR training procedure job");
        final Optional<OcrTrainingLangStats> trainingLangStats = ocrTrainingLangStatsRepository
                .findFirstByNewExamplesGreaterThanEqualOrderByNewExamplesDesc(
                        TRAINING_MIN_NEW_EXAMPLES);
        if (trainingLangStats.isEmpty()) {
            log.info("No language candidate for OCR training");
            return;
        }
        try {
            launchTrainingProcedure(trainingLangStats.get());
        } catch (OcrTrainingAlreadyInProgressException e) {
            log.info("Training procedure is already in progress, skipping");
        }
    }

    /**
     * Starts training procedure for model for given language
     * @param lang language code of the desired model's language
     * @throws OcrTrainingAlreadyInProgressException some training procedure is
     *                                               already launched and only one
     *                                               can be launched at the same
     *                                               time
     */
    public void startTrainingProcedure(String lang) throws OcrTrainingAlreadyInProgressException {
        OcrTrainingLangStats trainingLangStats = ocrTrainingLangStatsRepository.findByLang(lang)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("No training data for language: %s", lang)));

        launchTrainingProcedure(trainingLangStats);
    }

    private void launchTrainingProcedure(OcrTrainingLangStats trainingLangStats)
            throws OcrTrainingAlreadyInProgressException {
        boolean hasLock = trainingProcedureLock.tryLock();
        if (!hasLock)
            throw new OcrTrainingAlreadyInProgressException();

        String trainingLang = trainingLangStats.getLang();
        log.info("Starting training session for lang: {}", trainingLang);

        try {
            executeTrainingProcedure(trainingLang);

            long processedExamples = trainingLangStats.getNewExamples();
            updateProcessedTrainingExamples(trainingLang, processedExamples);

            log.info("Training session finished");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Training session interrupted", e);
        } finally {
            trainingProcedureLock.unlock();
        }

    }

    private void updateProcessedTrainingExamples(String trainingLang, long processedExamples) {
        try {
            OcrTrainingLangStats stats = ocrTrainingLangStatsRepository.findByLang(trainingLang)
                    .orElseThrow(() -> new RuntimeException("Unexpected lack of training language stats"));

            stats.notifyUsedExamples(processedExamples);
            ocrTrainingLangStatsRepository.save(stats);
        }
        catch (OptimisticLockException e) {
            log.warn("Failed to update OCR training lang stats due to optimistic locking failure", e);
        }
    }

    private void executeTrainingProcedure(String lang) throws InterruptedException {

        final String startModel = lang.replaceFirst("(.*)_.*", "$1");

        ProcessBuilder processBuilder = new ProcessBuilder(
                "make",
                "training",
                "TESSDATA=" + tessdataDir,
                "LANGDATA_DIR=" + langdataDir,
                "GROUND_TRUTH_DIR=" + toGroundTruthPath(lang).toString(),
                "DATA_DIR=" + trainedLangsDir,
                "START_MODEL=" + startModel,
                "MODEL_NAME=" + lang,
                "MAX_ITERATIONS=" + Integer.toString(maxTrainingIterations),
                "RANDOM_SEED=" + Long.toString(new Random().nextLong()))
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT);

        try {
            Process trainingProcess = processBuilder.start();
            int exitCode = trainingProcess.waitFor();

            if (exitCode != 0)
                throw new RuntimeException(
                        String.format("Training procedure exited with non-zero (%d) exit status", exitCode));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Path toGroundTruthPath(String lang) {
        return Path.of(trainedLangsDir, lang, "ground-truth");
    }

    void incrementLangStatsNewExamplesNum(String lang, long newExamplesNum) {
        OcrTrainingLangStats newStats = ocrTrainingLangStatsRepository.findByLang(lang)
                .orElseGet(() -> new OcrTrainingLangStats(lang));

        newStats.notifyNewExamples(newExamplesNum);
        ocrTrainingLangStatsRepository.save(newStats);
    }
}
