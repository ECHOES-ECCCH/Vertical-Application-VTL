package pl.psnc.vtl.iiif.eBooks;

import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

public interface EBookExporter {

    CompletableFuture<Void> exportAndGetLocation(MultipartFile[] files, long transcriptionId, String title,
            String author, String hash, String jsonMappings, String titlePage);
}
