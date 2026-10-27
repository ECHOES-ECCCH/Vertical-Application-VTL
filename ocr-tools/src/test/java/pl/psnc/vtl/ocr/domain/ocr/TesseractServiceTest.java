package pl.psnc.vtl.ocr.domain.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.junit.Test;
import pl.psnc.vtl.ocr.api.error.VtlOcrException;
import pl.psnc.vtl.ocr.api.ocr.tesseract.VerseBboxDto;
import pl.psnc.vtl.ocr.api.ocr.tesseract.VerseBboxesDto;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TesseractServiceTest {

    @Test
    public void shouldReocognizeHocrOnValidPage() throws TesseractException, VtlOcrException {
        Tesseract tess = mock(Tesseract.class);
        when(tess.doOCR(any(BufferedImage.class)))
                .thenReturn("<example-hocr>");

        TesseractFactory tesseractFactory = mock(TesseractFactory.class);
        when(tesseractFactory.createHocrTesseract(any(String.class), same(false)))
                .thenReturn(tess);

        OcrLanguageRegistry ocrLanguageRegistry = mock(OcrLanguageRegistry.class);
        when(ocrLanguageRegistry.isLangSupported(anyString()))
                .thenReturn(true);

        TranscriptionImageProvider imageProvider = mock(TranscriptionImageProvider.class);
        when(imageProvider.getImage(anyLong(), anyString(), anyInt()))
                .thenReturn(mock(BufferedImage.class));
        TesseractService tesseractService =
                new TesseractService(imageProvider, tesseractFactory, ocrLanguageRegistry, null);

        assertNotNull(tesseractService.scanImageHocr(1, "test-file", 1, "eng", false));
    }

    @Test
    public void shouldRecognizeSubimageBatchOnValidPage() throws TesseractException, VtlOcrException {
        Tesseract tess = mock(Tesseract.class);
        when(tess.doOCR(any(BufferedImage.class), any(Rectangle.class)))
                .thenReturn("<example-text>");

        TesseractFactory tesseractFactory = mock(TesseractFactory.class);
        when(tesseractFactory.createPlainTesseract(any(String.class), same(false)))
                .thenReturn(tess);

        OcrLanguageRegistry ocrLanguageRegistry = mock(OcrLanguageRegistry.class);
        when(ocrLanguageRegistry.isLangSupported(anyString()))
                .thenReturn(true);

        TranscriptionImageProvider imageProvider = mock(TranscriptionImageProvider.class);
        when(imageProvider.getImage(anyLong(), anyString(), anyInt()))
                .thenReturn(mock(BufferedImage.class));
        TesseractService tesseractService =
                new TesseractService(imageProvider, tesseractFactory, ocrLanguageRegistry, null);

        VerseBboxesDto verses = new VerseBboxesDto(
                Arrays.asList(
                        new VerseBboxDto(1L, 100, 100, 200, 150),
                        new VerseBboxDto(2L, 300, 100, 400, 150),
                        new VerseBboxDto(3L, 100, 200, 200, 250),
                        new VerseBboxDto(4L, 300, 200, 400, 250)
                )
        );

        Map<Long, String> expectedContent = new HashMap<>();
        expectedContent.put(1L, "<example-text>");
        expectedContent.put(2L, "<example-text>");
        expectedContent.put(3L, "<example-text>");
        expectedContent.put(4L, "<example-text>");

        Map<Long, String> versesContent =
                tesseractService.scanSubImageBatchPlain(1, "test-file", 1, verses, "eng", false, false);

        assertEquals(expectedContent, new HashMap<>(versesContent));
    }
}