package pl.psnc.vtl.portal.services.pages;

import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TranscriptionZipAction {


    @FunctionalInterface
    public interface ZipActionConsumer{
        /**
         * @param fileName  fileName name of the file inside zip
         * @param transcriptionId id of transcription on what you want to perform an action
         * @param inputStream content of the file with given name
         */
        void accept(String fileName, long transcriptionId, InputStream inputStream) throws IOException;
    }


    @SneakyThrows
    public static void performOnZipContent(Long transcriptionId, MultipartFile multipartFile, ZipActionConsumer consumer) {
        if (multipartFile.getOriginalFilename().endsWith(".zip")) {
            try (ZipInputStream zis = new ZipInputStream(multipartFile.getInputStream())) {
                ZipEntry ze = zis.getNextEntry();
                while (ze != null) {
                    try {
                        consumer.accept(ze.getName(), transcriptionId, zis);
                    } finally {
                        zis.closeEntry();
                    }
                    ze = zis.getNextEntry();
                }
            }
        } else throw ZipImportExceptionFactory.unsupportableFileTypeException(multipartFile.getOriginalFilename());
    }
}
