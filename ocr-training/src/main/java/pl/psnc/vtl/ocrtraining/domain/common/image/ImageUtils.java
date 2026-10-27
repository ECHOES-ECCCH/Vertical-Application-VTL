package pl.psnc.vtl.ocrtraining.domain.common.image;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.springframework.http.MediaType;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


@UtilityClass
public class ImageUtils {

    public MediaType resolveMediaType(@NonNull ImageMediaType imgMediaType) {
        switch (imgMediaType) {
            case JPEG:
                return MediaType.IMAGE_JPEG;

            case PNG:
                return MediaType.IMAGE_PNG;

            default:
                throw new IllegalArgumentException(String.format("Illegal image media type: %s", imgMediaType.name()));
        }
    }

    public byte[] convertImage(byte[] imageContents, ImageMediaType to) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageContents);
        BufferedImage bufferedImage = ImageIO.read(inputStream);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        boolean success =  ImageIO.write(bufferedImage, to.getFileExtension(), outputStream);

        if (!success)
            throw new RuntimeException(String.format("Given image format (%s) is not supported", to.getFileExtension()));

        return outputStream.toByteArray();
    }
}
