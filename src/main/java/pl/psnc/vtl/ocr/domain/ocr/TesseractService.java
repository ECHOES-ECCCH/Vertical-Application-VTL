package pl.psnc.vtl.ocr.domain.ocr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.ocr.api.error.VtlOcrException;
import pl.psnc.vtl.ocr.api.ocr.tesseract.VerseBboxDto;
import pl.psnc.vtl.ocr.api.ocr.tesseract.VerseBboxesDto;
import pl.psnc.vtl.ocr.domain.ocr.exception.*;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TesseractService {

    private final TranscriptionImageProvider transcriptionImageProvider;
    private final TesseractFactory tesseractFactory;
    private final OcrLanguageRegistry ocrLanguageRegistry;
    private final OcrTrainingClientService ocrTrainingClientService;

    private volatile int taskCounter = 0;

    private synchronized void startTask() {
        taskCounter++;
    }

    private synchronized void finishTask() {
        taskCounter--;
    }

    public List<String> getTrainedLanguages() {
        List<String> langs = ocrTrainingClientService.getTrainableLanguages();
        langs.removeIf(lang -> !ocrLanguageRegistry.isLangSupported(lang));
        return langs;
    }

    /**
     * Performs OCR scan on whole page and returns the result in hOCR format
     * @param page          number of the page to scan in the image indexed starting
     *                      from 1
     * @param lang          language parameter which specifies which character set
     *                      and LSTM will be used while performing the OCR
     * @param handleColumns if true, use more advanced segmentation mode that
     *                      handles columns
     * @return string containing hOCR scan
     * @throws VtlOcrException represents general expected error
     */
    public String scanImageHocr(long transcriptionId, String filename, int page,
            String langCode, boolean handleColumns)
            throws VtlOcrException {
        try {
            startTask();
            String lang = getValidatedLang(langCode);

            Tesseract tess = tesseractFactory.createHocrTesseract(lang, handleColumns);
            BufferedImage image = transcriptionImageProvider.getImage(transcriptionId, filename, page);
            return tess.doOCR(image);
        } catch (TesseractException e) {
            throw new OcrRecognitionException(filename, page, null, e);
        } finally {
            finishTask();
        }

    }

    /**
     * Performs OCR scan on multiple sub-image regions
     * @param imageVerses handles to image and its sub-regions on which to perform
     *                    the scan
     * @param page        number of the page to scan in the image indexed starting
     *                    from 1
     * @param lang        language parameter which specifies which character set and
     *                    LSTM will be used while performing the OCR
     * @param hocr        if true, OCR returns the result in hOCR (for word-level
     *                    coordinates)
     * @param singleWord  if true, OCR assumes that each sub-image contains a single
     *                    word. Only valid if hocr is false
     * @return dictionary containing verse id as keys and verse content (inside
     *         bounding boxes) as values
     * @throws VtlOcrException represents general expected error
     */
    public Map<Long, String> scanSubImageBatchPlain(long transcriptionId, String filename, int page,
            VerseBboxesDto verseBboxes, String langCode,
            boolean hocr, boolean singleWord) throws VtlOcrException {

        try {
            startTask();
            String lang = getValidatedLang(langCode);
            Tesseract tess = hocr ? tesseractFactory.createLineHocrTesseract(lang)
                    : tesseractFactory.createPlainTesseract(lang, singleWord);

            BufferedImage imagePage = transcriptionImageProvider.getImage(transcriptionId, filename, page);
            Map<Long, String> versesContent = new HashMap<>();
            for (VerseBboxDto verse : verseBboxes.getVerses()) {
                try {
                    Rectangle rect = new Rectangle(verse.getX1(), verse.getY1(),
                            verse.getX2() - verse.getX1(),
                            verse.getY2() - verse.getY1());
                    String content = tess.doOCR(imagePage, rect);
                    content = getStringWithRemovedDashesAndReplacedEOLWithSpace(content);
                    versesContent.put(verse.getId(), content);
                } catch (TesseractException e) {
                    throw new OcrRecognitionException(filename, page, verse, e);
                }
            }
            return versesContent;
        } finally {
            finishTask();
        }
    }

    public boolean isBusy() {
        return taskCounter > 0;
    }

    public int taskCounter() {
        return taskCounter;
    }


    private String getStringWithRemovedDashesAndReplacedEOLWithSpace(String content) {
        content = content.replace("\u002D\n", "").replace("\u2010\n", "").replace("\u2013\n", "").trim()
                .replace("\n", " ");
        return content;
    }

    private String getValidatedLang(String langCode) throws OcrUnsupportedLanguageException {
        if (!ocrLanguageRegistry.isLangSupported(langCode))
            throw new OcrUnsupportedLanguageException(langCode);
        return langCode;
    }
}
