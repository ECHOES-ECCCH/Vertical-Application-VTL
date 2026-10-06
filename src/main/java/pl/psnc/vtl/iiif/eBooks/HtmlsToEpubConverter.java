package pl.psnc.vtl.iiif.eBooks;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Component
@Slf4j
@RequiredArgsConstructor
class HtmlsToEpubConverter implements EBookExporter {

    @Value("${vtl.iiif-tools.storage}")
    private String rootFolder;

    @Autowired
    @Qualifier("applicationTaskExecutor")
    private AsyncTaskExecutor asyncExecutor;

    private final EpubCreator epubCreator = new EpubCreator();
    private final EpubWriter epubWriter = new EpubWriter();

    @SneakyThrows
    @Override
    public CompletableFuture<Void> exportAndGetLocation(MultipartFile[] files, long transcriptionId, String title,
            String author, String hash, String json, String titlePage) {
        Book book = epubCreator.getBook(files, title, author, json);
        Path eBookPath = EBookLocation.eBookPath(EBookFormat.EPUB, transcriptionId, hash);
        Path eBookFullPath = Path.of(rootFolder).resolve(eBookPath);
        
        return CompletableFuture.runAsync(() -> {
            try {
                Files.createDirectories(eBookFullPath.getParent());

                try (FileOutputStream fos = new FileOutputStream(eBookFullPath.toFile())) {
                    epubWriter.write(book, fos);
                }
            } catch (Exception e) {
                log.error("Failed to export " + eBookPath, e);
                throw new CompletionException(e);
            }
        }, asyncExecutor);
    }
}
