package pl.psnc.vtl.ocr.domain.ocr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITessAPI.TessOcrEngineMode;
import net.sourceforge.tess4j.ITessAPI.TessPageSegMode;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import pl.psnc.vtl.ocr.api.ocr.VerseBboxesDto.VerseBboxDto;
import pl.psnc.vtl.ocr.domain.ocr.exception.InvalidTranscriptionPageException;
import pl.psnc.vtl.ocr.domain.ocr.exception.OcrRecognitionException;
import pl.psnc.vtl.ocr.domain.ocr.exception.OcrUnsupportedLanguageException;
import pl.psnc.vtl.ocr.domain.ocr.exception.TranscriptionImageLoadException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
class TesseractWrapper implements OcrWrapper {

    private final Tesseract tesseract;
    private final Path modelsTempDir;

    public static TesseractWrapper createHocrTesseract(Path modelsDir, String lang, boolean handleColumns)
            throws IOException {
        TesseractWrapper wrapper = baseTesseractHandle(modelsDir, lang);
        wrapper.tesseract.setVariable("tessedit_create_hocr", String.valueOf(ITessAPI.TRUE));
        wrapper.tesseract.setPageSegMode(handleColumns ? TessPageSegMode.PSM_AUTO : TessPageSegMode.PSM_SINGLE_BLOCK);
        return wrapper;
    }

    public static TesseractWrapper createLineHocrTesseract(Path modelsDir, String lang) throws IOException {
        TesseractWrapper wrapper = baseTesseractHandle(modelsDir, lang);
        wrapper.tesseract.setVariable("tessedit_create_hocr", String.valueOf(ITessAPI.TRUE));
        wrapper.tesseract.setPageSegMode(TessPageSegMode.PSM_SINGLE_LINE);
        return wrapper;
    }

    public static TesseractWrapper createPlainTesseract(Path modelsDir, String lang, boolean singleWord)
            throws IOException {
        TesseractWrapper wrapper = baseTesseractHandle(modelsDir, lang);
        wrapper.tesseract
                .setPageSegMode(singleWord ? TessPageSegMode.PSM_SINGLE_WORD : TessPageSegMode.PSM_SINGLE_LINE);
        return wrapper;
    }

    private static TesseractWrapper baseTesseractHandle(Path modelsDir, String models) throws IOException {
        Tesseract tesseract = new FixedTesseract();

        log.debug("Creating Tesseract handle with models: {}", models);

        Path modelsTempDir = Files.createTempDirectory("tesseract");
        String[] modelPaths = models.split("[+]");
        StringBuilder tessLang = new StringBuilder();
        for (int i = 0; i < modelPaths.length; i++) {
            String modelPath = modelPaths[i];
            Path modelFile = modelsDir.resolve(modelPath);
            if (!Files.exists(modelFile)) {
                throw new OcrUnsupportedLanguageException(modelPath);
            }
            String linkName = "" + ('a' + i);
            tessLang.append(i > 0 ? "+" : "").append(linkName);
            Files.createSymbolicLink(modelsTempDir.resolve(linkName + ".traineddata"), modelFile);
        }

        tesseract.setDatapath(modelsTempDir.toString());
        tesseract.setLanguage(tessLang.toString());
        tesseract.setOcrEngineMode(TessOcrEngineMode.OEM_LSTM_ONLY);

        return new TesseractWrapper(tesseract, modelsTempDir);
    }

    @Override
    public void close() throws IOException {
        FileUtils.deleteDirectory(modelsTempDir.toFile());
    }

    @Override
    public String doOcr(Path imagePath, Integer page) {
        BufferedImage image = getImage(imagePath, page);
        try {
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            throw new OcrRecognitionException(imagePath.toString(), page, null, e);
        }
    }

    @Override
    public List<String> doOcr(Path imagePath, Integer page, List<VerseBboxDto> regions) {
        BufferedImage image = getImage(imagePath, page);
        List<String> result = new ArrayList<>(regions.size());
        for (VerseBboxDto region : regions) {
            Rectangle rect = new Rectangle(region.getX1(), region.getY1(),
                    region.getX2() - region.getX1(),
                    region.getY2() - region.getY1());
            try {
                result.add(tesseract.doOCR(image, rect));
            } catch (TesseractException e) {
                throw new OcrRecognitionException(imagePath.toString(), page, null, e);
            }
        }
        return result;
    }

    private BufferedImage getImage(Path imagePath, Integer page) {
        try {
            String fileName = imagePath.getFileName().toString();
            if (fileName.toLowerCase().endsWith(".pdf")) {
                return getImagePdf(imagePath, page);
            }

            Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(
                    fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase());
            if (!readers.hasNext())
                throw new TranscriptionImageLoadException(fileName, page);

            try (ImageInputStream iis = ImageIO.createImageInputStream(imagePath.toFile())) {
                ImageReader reader = readers.next();
                reader.setInput(iis);
                if (page < 0 || page > reader.getNumImages(true))
                    throw new InvalidTranscriptionPageException(fileName, page);
                return reader.read(page - 1);
            } catch (IOException e) {
                throw new TranscriptionImageLoadException(fileName, page, e);
            }
        } catch (IOException e) {
            throw new TranscriptionImageLoadException(imagePath.toString(), page, e);
        }
    }

    private BufferedImage getImagePdf(Path imagePath, Integer page) throws IOException {
        try (var buffered = new RandomAccessReadBufferedFile(imagePath);
                PDDocument document = Loader.loadPDF(buffered)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            if (page < 0 || page > document.getNumberOfPages())
                throw new InvalidTranscriptionPageException(imagePath.getFileName().toString(), page);
            return pdfRenderer.renderImageWithDPI(page - 1, 300, ImageType.RGB);
        }
    }
}
