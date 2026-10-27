package pl.psnc.vtl.ocr.domain.ocr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;


@Component
public class TesseractModelsStorage {

    @Value("${tesseract.tessdata}")
    private Path tessdataPath;
    @Value("${vtl.ocr-training.trained-langs-dir}")
    private Path trainedLangsDirPath;

    public Path getTessdataPath(String lang) {
        List<String> langs = Arrays.asList(lang.split("\\+"));
        if (langs.stream().allMatch(this::isTrained))
            return trainedLangsDirPath;
        if (langs.stream().allMatch(this::isTessdata))
            return tessdataPath;
        if (!langs.stream().allMatch(l -> isTrained(l) || isTessdata(l)))
            return null;

        for (String lan : langs) {
            if (!isTrained(lan)) {
                try {
                    Files.copy(tessdataPath.resolve(lang + ".traineddata"),
                            trainedLangsDirPath.resolve(lang + ".traineddata"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return trainedLangsDirPath;
    }

    private boolean isTrained(String lang) {
        return Files.exists(trainedLangsDirPath.resolve(lang + ".traineddata"));
    }

    private boolean isTessdata(String lang) {
        return Files.exists(tessdataPath.resolve(lang + ".traineddata"));
    }
}
