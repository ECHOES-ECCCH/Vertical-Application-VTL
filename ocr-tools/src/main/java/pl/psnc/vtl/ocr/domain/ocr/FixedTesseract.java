package pl.psnc.vtl.ocr.domain.ocr;

import net.sourceforge.lept4j.Pix;
import net.sourceforge.lept4j.util.LeptUtils;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.ImageIOHelper;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import static net.sourceforge.tess4j.ITessAPI.TRUE;

/**
 * Uses Pix instead of ByteBuffer
 * @see https://github.com/nguyenq/tess4j/issues/268
 */
class FixedTesseract extends Tesseract {

    private Properties prop = new Properties();

    @Override
    public void setVariable(String key, String value) {
        prop.setProperty(key, value);
        super.setVariable(key, value);
    }

    @Override
    public String doOCR(File inputFile, List<Rectangle> rects) throws TesseractException {
        try {
            File imageFile = ImageIOHelper.getImageFile(inputFile);
            String imageFileFormat = ImageIOHelper.getImageFileFormat(imageFile);
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(imageFileFormat);
            if (!readers.hasNext()) {
                throw new RuntimeException(ImageIOHelper.JAI_IMAGE_READER_MESSAGE);
            }
            ImageReader reader = readers.next();
            StringBuilder result = new StringBuilder();
            try (ImageInputStream iis = ImageIO.createImageInputStream(imageFile);) {
                reader.setInput(iis);
                int imageTotal = reader.getNumImages(true);

                init();
                setVariables();

                for (int i = 0; i < imageTotal; i++) {
                    BufferedImage bi = reader.read(i);

                    result.append(doOCR(bi, inputFile.getPath(), rects, i + 1));
                }

                if (String.valueOf(TRUE).equals(prop.getProperty("tessedit_create_hocr"))) {
                    result.insert(0, htmlBeginTag).append(htmlEndTag);
                }
            } finally {
                // delete temporary TIFF image for PDF
                if (imageFile != null && imageFile.exists() && imageFile != inputFile
                        && imageFile.getName().startsWith("multipage")
                        && imageFile.getName().endsWith(ImageIOHelper.TIFF_EXT)) {
                    imageFile.delete();
                }
                reader.dispose();
                dispose();
            }

            return result.toString();
        } catch (IOException e) {
            throw new TesseractException(e);
        }
    }

    @Override
    public String doOCR(BufferedImage bi, String filename, List<Rectangle> rects) throws TesseractException {
        init();
        setVariables();
        return doOCR(bi, filename, rects, 1);
    }

    private String doOCR(BufferedImage bi, String filename, List<Rectangle> rects, int pageNum)
            throws TesseractException {
        Pix image;
        try {
            image = LeptUtils.convertImageToPix(bi);
        } catch (IOException e) {
            throw new TesseractException("Image conversion error", e);
        }
        return doOCR(image, filename, rects, pageNum);
    }

    private String doOCR(Pix image, String filename, List<Rectangle> rois, int pageNum) {
        StringBuilder sb = new StringBuilder();

        setImage(image);
        if (rois != null && !rois.isEmpty()) {
            for (Rectangle rect : rois) {
                setROI(rect);
                sb.append(getOCRText(filename, pageNum));
            }
        } else {
            sb.append(getOCRText(filename, pageNum));
        }

        return sb.toString();
    }

    private void setImage(Pix image) {
        getAPI().TessBaseAPISetImage2(getHandle(), image);
    }
}