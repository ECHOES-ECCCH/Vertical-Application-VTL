package pl.psnc.vtl.iiif.eBooks;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EBookservice {

    enum Status {
        READY,
        PROCESSING,
        FAILED,
        NOT_EXISTS;
    }

    @lombok.Value
    @AllArgsConstructor
    static final class ExportedEBook {
        Status status;
        String details;
    }

    @Value("${vtl.iiif-tools.storage}")
    private String rootFolder;

    private final HtmlsToEpubConverter htmlsToEpubConverter;
    private final PdfExporter pdfExporter;
    private final MobiGenerator mobiGenerator;

    private Map<Path, CompletableFuture<Void>> exportTasks = new ConcurrentHashMap<>();

    public ExportedEBook getExportedEBook(Long transcriptionId, EBookFormat eBookFormat, String hash) {
        Path eBookPath = EBookLocation.eBookPath(eBookFormat, transcriptionId, hash);
        Path eBookFullPath = Path.of(rootFolder).resolve(eBookPath);
        if (Files.exists(eBookFullPath)) {
            exportTasks.remove(eBookPath);
            return new ExportedEBook(Status.READY, eBookPath.toString());
        }
        CompletableFuture<Void> task = exportTasks.get(eBookPath);
        if (task == null)
            return new ExportedEBook(Status.NOT_EXISTS, null);
        if (!task.isDone())
            return new ExportedEBook(Status.PROCESSING, null);
        try {
            task.getNow(null);
            exportTasks.remove(eBookPath);
            if (!Files.exists(eBookFullPath))
                return new ExportedEBook(Status.FAILED, "Processing done but file missing");
            return new ExportedEBook(Status.READY, eBookPath.toString());
        } catch (CompletionException e) {
            return new ExportedEBook(Status.FAILED, e.getCause().getMessage());
        }
    }

    public void exportAndGetLocation(EBookFormat format,
                                       MultipartFile[] files, long transcriptionId, String title, String author, String hash, String jsonMappings, String titlePage) {
        Path eBookPath = EBookLocation.eBookPath(format, transcriptionId, hash);
        CompletableFuture<Void> task = exportTasks.get(eBookPath);
        if (task != null && !task.isDone())
            return;
        task = getEBookExporter(format)
                .exportAndGetLocation(files, transcriptionId, title, author, hash, jsonMappings, titlePage);
        exportTasks.put(eBookPath, task);
    }

    private EBookExporter getEBookExporter(EBookFormat format) {
        switch (format) {
        case EPUB:
            return htmlsToEpubConverter;
        case PDF:
            return pdfExporter;
        case MOBI:
            return mobiGenerator;
        default:
            throw new IllegalArgumentException("Unknown format: " + format);
        }
    }
}

