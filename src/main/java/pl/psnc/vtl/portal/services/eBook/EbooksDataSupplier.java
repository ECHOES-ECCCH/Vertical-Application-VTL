package pl.psnc.vtl.portal.services.eBook;

import lombok.AllArgsConstructor;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.entity.ContentType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.services.eBook.HtmlBody.IndexAnchor;
import pl.psnc.vtl.portal.services.imports.BeanLoader;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionHelper;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionService;
import pl.psnc.vtl.portal.services.transcriptions.content.SectionHTMLTranscriptionContentGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@AllArgsConstructor
public class EbooksDataSupplier {

    private final SectionHTMLTranscriptionContentGenerator sectionHTMLTranscriptionContentGenerator;

    public String getTitle(Transcription transcription) {
        return TranscriptionHelper.findTitleLabel(transcription);
    }


    public String getAuthor(Transcription transcription) {
        return TranscriptionHelper.findAuthorsLabel(transcription);
    }

    public ImmutablePair<List<MultipartFile>, HashMap<String, String>> getHtmlFiles(Transcription transcription,
            Layer layer, String indexLanguage) throws IOException {

        List<MultipartFile> htmlFiles = new ArrayList<>();
        HashMap<String, String> pagesIiifUrls = new HashMap<>();
        Map<IndexAnchor, String> indexAnchors = new HashMap<>();

        List<HtmlBody> htmlBodies = sectionHTMLTranscriptionContentGenerator
                .getHTMLBody(transcription, getTitle(transcription),layer);

        int fileCount = 1;

        for (HtmlBody htmlBody : htmlBodies) {
            pagesIiifUrls.putAll(htmlBody.getPagesIiifUrls());

            String filename = htmlBody.getSectionLevel() + "fileNo" + fileCount + ".xhtml";
            fileCount++;
            htmlFiles.add(createFile(filename, htmlBody.getContent()));

            htmlBody.getIndexAnchors().forEach(anchor -> indexAnchors.put(anchor, filename));
        }
        
        if (!indexAnchors.isEmpty()) {
            if (indexLanguage == null) {
                var transcriptionService = BeanLoader.getBean(TranscriptionService.class);
                indexLanguage = transcriptionService.getTranscriptionDefaultLanguage(transcription.getId());
            }
            if (indexLanguage != null) {
                String body = sectionHTMLTranscriptionContentGenerator.prepareIndexBody(indexAnchors, indexLanguage);
                htmlFiles.add(createFile("1index.xhtml", body));
            }
        }

        return new ImmutablePair<>(htmlFiles, pagesIiifUrls);
    }

    private CommonsMultipartFile createFile(String filename, String content) throws IOException {
        DiskFileItemFactory factory =
                new DiskFileItemFactory(1024 * 1024, Files.createTempDirectory("tmp_xhtml").toFile());
        FileItem fileItem = factory.createItem("files", ContentType.MULTIPART_FORM_DATA.toString(), false, filename);
        try (OutputStream outputStream = fileItem.getOutputStream()) {
            outputStream.write(content.getBytes(UTF_8));
        }
        return new CommonsMultipartFile(fileItem);
    }
}
