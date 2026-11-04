package pl.psnc.vtl.ocr.domain.ocr;

import pl.psnc.vtl.ocr.api.ocr.VerseBboxesDto.VerseBboxDto;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface OcrWrapper extends AutoCloseable {

    String doOcr(Path imagePath, Integer page) throws IOException;

    List<String> doOcr(Path imagePath, Integer page, List<VerseBboxDto> regions) throws IOException;

    @Override
    default void close() throws IOException {
        // empty by default
    }
}
