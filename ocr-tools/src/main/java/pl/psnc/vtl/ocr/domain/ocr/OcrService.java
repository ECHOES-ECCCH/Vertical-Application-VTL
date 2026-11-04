package pl.psnc.vtl.ocr.domain.ocr;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.ocr.api.error.VtlOcrException;
import pl.psnc.vtl.ocr.api.ocr.VerseBboxesDto;
import pl.psnc.vtl.ocr.domain.ocr.exception.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OcrService {

    public enum Engine {
        TESSERACT,
        KRAKEN;

        static Engine of(String value) {
            try {
                return Engine.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new OcrUnsupportedLanguageException(value);
            }
        }
    }

    @Value("${vtl.ocr-tools.storage}")
    private String rootFolder;

    @Value("${vtl.ocr-models-dir}")
    private Path modelsDir;

    private volatile int taskCounter = 0;

    private synchronized void startTask() {
        taskCounter++;
    }

    private synchronized void finishTask() {
        taskCounter--;
    }

    /**
     * Performs OCR scan on whole page and returns the result in hOCR format
     * @param page          number of the page to scan in the image indexed starting
     *                      from 1
     * @param engine        OCR engine to use: kraken or tesseract
     * @param model         OCR model relative path
     * @param handleColumns if true, use more advanced segmentation mode that
     *                      handles columns
     * @return string containing hOCR scan
     * @throws VtlOcrException represents general expected error
     */
    public String scanImageHocr(long transcriptionId, String filename, int page,
            String engine, String model, boolean handleColumns)
            throws VtlOcrException {
        try {
            startTask();
            try (OcrWrapper ocr = createHocrWrapper(engine, model, handleColumns)) {
                Path imagePath = findImagePath(transcriptionId, filename);
                return ocr.doOcr(imagePath, page);
            }
        } catch (IOException e) {
            throw new OcrRecognitionException(filename, page, null, e);
        } finally {
            finishTask();
        }

    }

    private OcrWrapper createHocrWrapper(String engine, String model, boolean handleColumns) throws IOException {
        switch (Engine.of(engine)) {
        case TESSERACT:
            return TesseractWrapper.createHocrTesseract(modelsDir, model, handleColumns);
        case KRAKEN:
            return new KrakenWrapper(modelsDir.resolve(model), true);
        }
        throw new OcrUnsupportedLanguageException(engine);
    }

    /**
     * Performs OCR scan on multiple sub-image regions
     * @param imageVerses handles to image and its sub-regions on which to perform
     *                    the scan
     * @param page        number of the page to scan in the image indexed starting
     *                    from 1
     * @param engine      OCR engine to use: kraken or tesseract
     * @param model       OCR model relative path
     * @param hocr        if true, OCR returns the result in hOCR (for word-level
     *                    coordinates)
     * @param singleWord  if true, OCR assumes that each sub-image contains a single
     *                    word. Only valid if hocr is false and engine is tesseract
     * @return dictionary containing verse id as keys and verse content (inside
     *         bounding boxes) as values
     * @throws VtlOcrException represents general expected error
     */
    public Map<Long, String> scanSubImageBatchPlain(long transcriptionId, String filename, int page,
            VerseBboxesDto verseBboxes, String engine, String model,
            boolean hocr, boolean singleWord) throws VtlOcrException {
        try {
            startTask();
            try (OcrWrapper ocr = createBatchWrapper(engine, model, hocr, singleWord)) {
                Path imagePath = findImagePath(transcriptionId, filename);
                List<String> resultList = ocr.doOcr(imagePath, page, verseBboxes.getVerses());
                Map<Long, String> result = new LinkedHashMap<>();
                for (int i = 0; i < resultList.size(); i++) {
                    String content = getStringWithRemovedDashesAndReplacedEOLWithSpace(resultList.get(i));
                    result.put(verseBboxes.getVerses().get(i).getId(), content);
                }
                return result;
            }
        } catch (IOException e) {
            throw new OcrRecognitionException(filename, page, null, e);
        } finally {
            finishTask();
        }
    }

    @SuppressWarnings("resource")
    private OcrWrapper createBatchWrapper(String engine, String model, boolean hocr, boolean singleWord)
            throws IOException {
        switch (Engine.of(engine)) {
        case TESSERACT:
            return hocr ? TesseractWrapper.createLineHocrTesseract(modelsDir, model)
                    : TesseractWrapper.createPlainTesseract(modelsDir, model, singleWord);
        case KRAKEN:
            return new KrakenWrapper(modelsDir.resolve(model), hocr);
        }
        throw new OcrUnsupportedLanguageException(engine);
    }

    public boolean isBusy() {
        return taskCounter > 0;
    }

    public int taskCounter() {
        return taskCounter;
    }

    private Path findImagePath(long transcriptionId, String fileName) {
        Path imagePath = Path.of(rootFolder, "originals", "t" + transcriptionId, fileName);
        if (!Files.isRegularFile(imagePath))
            imagePath = Path.of(rootFolder, "pyramids", "t" + transcriptionId, fileName);
        if (!Files.isRegularFile(imagePath))
            throw new TranscriptionImageNotFoundException(transcriptionId, fileName);
        return imagePath;
    }

    private String getStringWithRemovedDashesAndReplacedEOLWithSpace(String content) {
        return content.replace("\u002D\n", "").replace("\u2010\n", "").replace("\u2013\n", "").trim()
                .replace("\n", " ");
    }
}
