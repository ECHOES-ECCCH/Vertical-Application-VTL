package pl.psnc.vtl.ocr.domain.ocr;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Component
@RequiredArgsConstructor
public class OcrLanguageRegistry {
    private Set<String> supportedLangs;
    private final TesseractModelsStorage tesseractModelsStorage;

    @PostConstruct
    private void initSupportedLangs() {
        ProcessBuilder processBuilder = new ProcessBuilder("tesseract", "--list-langs");
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            try (InputStream inputStream = process.getInputStream()) {
                supportedLangs = new HashSet<>(IOUtils.readLines(inputStream, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isLangSupported(String language) {
        return Arrays.stream(language.split("\\+")).allMatch(
                lang -> supportedLangs.contains(lang) || tesseractModelsStorage.getTessdataPath(lang) != null);
    }
}
