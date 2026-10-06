package pl.psnc.vtl.portal.services.eBook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.http.entity.ContentType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import pl.psnc.vtl.portal.dto.eBook.PdfTitlePage;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.iiiftools.IIIFToolsRepository;
import pl.psnc.vtl.portal.repositories.layers.LayerRepository;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionRepository;
import pl.psnc.vtl.portal.services.iiifserver.IIIFUriResolver;
import pl.psnc.vtl.portal.services.pages.content.HOCRTranscriptionPageContentGenerator;

import javax.persistence.EntityNotFoundException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PdfExporter implements EBookExporter {

    private final IIIFToolsRepository iiifToolsRepository;

    private final TranscriptionRepository transcriptionRepository;

    private final PageRepository pageRepository;

    private final LayerRepository layerRepository;

    private final HOCRTranscriptionPageContentGenerator hocrTranscriptionPageContentGenerator;

    private final IIIFUriResolver iiifUriResolver;

    @Override
    public void exportEbook(EbookDto edto, String hash) {
        try {
            List<Page> pages = pageRepository.findByTranscriptionIdOrderByPageNo(edto.getTranscriptionId());

            List<MultipartFile> hocrs = new ArrayList<>();
            HashMap<String, String> pageImagesIiifUrls = new HashMap<>();

            Layer layer = layerRepository.findById(edto.getLayerId()).orElse(null);
            if (pages.isEmpty() || layer == null) {
                throw new EntityNotFoundException(
                        "No pageLayers available for export in transcription " + edto.getTranscriptionId());
            }

            for (Page p : pages) {
                String content = hocrTranscriptionPageContentGenerator.generateForPdf(p, layer);

                String hocrBasename = String.format("Page%04d", (p.getPageNo() - 1));
                String hocrFileName = hocrBasename + ".html";

                String iiifServerFileUrl = iiifUriResolver.getCompressedImageUrl(p);

                MultipartFile htmlFile = createMultipartFile(edto.getTranscriptionId(), content, hocrFileName);

                hocrs.add(htmlFile);

                pageImagesIiifUrls.put(hocrFileName, iiifServerFileUrl);
            }

            PdfTitlePage titlePage = null;
            if (edto.isCreateTitlePage()) {
                titlePage = createPdfTitlePage(edto.getTranscriptionId());
            }

            iiifToolsRepository.saveEbook(edto.getTranscriptionId(), hash, hocrs,
                    "WLT-T" + edto.getTranscriptionId(), "WLT-PDF", Ebook.PDF, pageImagesIiifUrls, titlePage);


        } catch (Exception e) {
            log.error("PDF export error for transcription (id={})", edto.getTranscriptionId(), e);
            throw new ConversionException(hash);
        }
    }

    private static MultipartFile createMultipartFile(long transcriptionId, String content, String fileName) throws IOException {
        DiskFileItemFactory factory = new DiskFileItemFactory(1024 * 1024,
                Files.createTempDirectory("tmp_xhtml").toFile());
        FileItem fileItem = factory.createItem("files", ContentType.MULTIPART_FORM_DATA.toString(), false,
                fileName);
        try (OutputStream outputStream = fileItem.getOutputStream()) {
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            log.error("Unable to create MultipartFile for one of HOCR files for exported transcription (id={})",
                    transcriptionId);
        }

        return new CommonsMultipartFile(fileItem);
    }

    private PdfTitlePage createPdfTitlePage(long transcriptionId) {
        PdfTitlePage titlePage = new PdfTitlePage();

        Transcription transcription = transcriptionRepository.findById(transcriptionId).orElseThrow(() -> new EntityNotFoundException(
                "Unable to find " + Transcription.class.getName() + " with id '" + transcriptionId + "'!"));
        titlePage.setTitle(transcription.getAnalyticInfo().getTitle());
        titlePage.setDescription(transcription.getTranscriptionInfo().getDescriptionPlain());
        titlePage.setKeywords(String.join("; ", transcription.getProfileDesc().getKeywords()));
        titlePage.setContact(transcription.getTranscriptionInfo().getContactName());

        return titlePage;
    }
}
