package pl.psnc.vtl.ocr.domain.ocr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITessAPI.TessOcrEngineMode;
import net.sourceforge.tess4j.ITessAPI.TessPageSegMode;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.stereotype.Component;
import pl.psnc.vtl.ocr.domain.ocr.exception.OcrUnsupportedLanguageException;

import java.nio.file.Path;


@Component
@RequiredArgsConstructor
@Slf4j
class TesseractFactory {

    private final TesseractModelsStorage tesseractModelsStorage;

    public Tesseract createHocrTesseract(String lang, boolean handleColumns) {
        Tesseract tesseract = baseTesseractHandle(lang);
        tesseract.setVariable("tessedit_create_hocr", String.valueOf(ITessAPI.TRUE));
        tesseract.setPageSegMode(handleColumns ? TessPageSegMode.PSM_AUTO : TessPageSegMode.PSM_SINGLE_BLOCK);
        return tesseract;
    }

    public Tesseract createLineHocrTesseract(String lang) {
        Tesseract tesseract = baseTesseractHandle(lang);
        tesseract.setVariable("tessedit_create_hocr", String.valueOf(ITessAPI.TRUE));
        tesseract.setPageSegMode(TessPageSegMode.PSM_SINGLE_LINE);
        return tesseract;
    }

    public Tesseract createPlainTesseract(String lang, boolean singleWord) {
        Tesseract tesseract = baseTesseractHandle(lang);
        tesseract.setPageSegMode(singleWord ? TessPageSegMode.PSM_SINGLE_WORD : TessPageSegMode.PSM_SINGLE_LINE);
        return tesseract;
    }

    private Tesseract baseTesseractHandle(String lang) {
        Tesseract tesseract = new FixedTesseract();
        Path tessdataPath = tesseractModelsStorage.getTessdataPath(lang);
        if (tessdataPath == null)
            throw new OcrUnsupportedLanguageException(lang);

        log.info("Creating Tesseract handle with tessdata path: {}", tessdataPath);

        tesseract.setDatapath(tessdataPath.toString());
        tesseract.setLanguage(lang);
        tesseract.setOcrEngineMode(TessOcrEngineMode.OEM_LSTM_ONLY);

        return tesseract;
    }
}
