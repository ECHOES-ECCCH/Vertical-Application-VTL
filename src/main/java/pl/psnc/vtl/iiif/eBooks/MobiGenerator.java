package pl.psnc.vtl.iiif.eBooks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.psnc.vtl.iiif.utils.CommandExecutor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Component
@RequiredArgsConstructor
@Slf4j
public class MobiGenerator implements EBookExporter {

    private static final String SCRIPT_FILE = "/epub_to_mobi.sh";

    @Value("${vtl.iiif-tools.storage}")
    private String rootFolder;

    @Value("${vtl.iiif-tools.scripts}")
    private String scriptsFolder;

    @Autowired
    @Qualifier("applicationTaskExecutor")
    private AsyncTaskExecutor asyncExecutor;

    private final HtmlsToEpubConverter htmlsToEpubConverter;

    private final CommandExecutor commandExecutor;

    @Override
    public CompletableFuture<Void> exportAndGetLocation(MultipartFile[] files, long transcriptionId, String title,
            String author, String hash, String mappings, String titlePage) {
        Path epubPath = EBookLocation.eBookPath(EBookFormat.EPUB, transcriptionId, hash);
        Path mobiPath = EBookLocation.eBookPath(EBookFormat.MOBI, transcriptionId, hash);
        CompletableFuture<Void> epubFuture = htmlsToEpubConverter.exportAndGetLocation(files, transcriptionId, title,
                author, hash, mappings, titlePage);
        return epubFuture.thenRunAsync(() -> {
            try {
                String epubConversionScript = scriptsFolder + SCRIPT_FILE;
                String epubsLocation = Path.of(rootFolder).resolve(epubPath.getParent()).toString();
                String epubName = epubPath.getFileName().toString();
                String mobiName = mobiPath.getFileName().toString();
                commandExecutor
                        .runCommand(Arrays.asList(epubConversionScript, epubsLocation, epubName, mobiName, "mobi"));
            } catch (Exception e) {
                log.error("Failed to export " + mobiPath, e);
                throw new CompletionException(e);
            }
        }, asyncExecutor);

    }
}
