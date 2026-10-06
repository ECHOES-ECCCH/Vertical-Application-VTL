package pl.psnc.vtl.ocr.domain.ocr;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.ocr.domain.ocr.exception.InvalidTranscriptionPageException;
import pl.psnc.vtl.ocr.domain.ocr.exception.TranscriptionImageLoadException;
import pl.psnc.vtl.ocr.domain.ocr.exception.TranscriptionImageNotFoundException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

@Service
public class TranscriptionImageProvider {

    @Value("${vtl.ocr-tools.storage}")
    private String rootFolder;

    public BufferedImage getImage(long transcriptionId, String filename, Integer page) {
        if (page < 1)
            throw new InvalidTranscriptionPageException(filename, page);
        try {
            File imageFile = new File(String.format("%s/originals/t%d/%s", rootFolder, transcriptionId, filename));
            if (!imageFile.exists())
                imageFile = new File(String.format("%s/pyramids/t%d/%s", rootFolder, transcriptionId, filename));
            if (!imageFile.exists())
                throw new TranscriptionImageNotFoundException(transcriptionId, filename);

            if (filename.toLowerCase().endsWith(".pdf")) {
                return getImagePdf(imageFile, page);
            }

            return getImage(imageFile, page);
        } catch (IOException e) {
            throw new TranscriptionImageLoadException(filename, page, e);
        }
    }

    private BufferedImage getImagePdf(File imageFile, Integer page) throws IOException {
        try (var buffered = new RandomAccessReadBufferedFile(imageFile);
                PDDocument document = Loader.loadPDF(buffered)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            if (page > document.getNumberOfPages())
                throw new InvalidTranscriptionPageException(imageFile.getName(), page);
            return pdfRenderer.renderImageWithDPI(page - 1, 300, ImageType.RGB);
        }
    }

    private BufferedImage getImage(File imageFile, Integer page) {
        Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(
                imageFile.getName().substring(imageFile.getName().lastIndexOf(".") + 1).toLowerCase());
        if (!readers.hasNext())
            throw new TranscriptionImageLoadException(imageFile.getName(), page);

        try (ImageInputStream iis = ImageIO.createImageInputStream(imageFile)) {
            ImageReader reader = readers.next();
            reader.setInput(iis);
            if (page > reader.getNumImages(true))
                throw new InvalidTranscriptionPageException(imageFile.getName(), page);
            return reader.read(page - 1);
        } catch (IOException e) {
            throw new TranscriptionImageLoadException(imageFile.getName(), page, e);
        }
    }

}
