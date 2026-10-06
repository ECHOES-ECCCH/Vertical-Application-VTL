package pl.psnc.vtl.iiif.eBooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.pdf.BaseFont;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;
import pl.psnc.vtl.iiif.converters.ConversionException;
import pl.psnc.vtl.iiif.utils.CommandExecutor;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Component
public class PdfExporter implements EBookExporter {

    private static final String CONVERT_TO_PDF_SCRIPT_FILE = "/hocr_to_pdf.sh";

    @Value("${vtl.iiif-tools.storage}")
    private String rootFolder;

    @Value("${vtl.iiif-tools.scripts}")
    private String scriptsFolder;

    @Value("${vtl.iiif-tools.cache}")
    private String cacheFolder;

    @Autowired
    @Qualifier("applicationTaskExecutor")
    private AsyncTaskExecutor asyncExecutor;

    private final CommandExecutor commandExecutor;

    private final TemplateEngine templateEngine;

    @Override
    public CompletableFuture<Void> exportAndGetLocation(MultipartFile[] hocrs, long transcriptionId, String title,
            String author, String hash, String mappingsJson, String titlePage) {
        Path eBookPath = EBookLocation.eBookPath(EBookFormat.PDF, transcriptionId, hash);
        Path eBookFolderPath = Path.of(rootFolder).resolve(eBookPath.getParent());
        String cacheLocation = cacheFolder + "/" + hash;

        File tmpDir = new File(cacheLocation);

        copyHocrFilesToTmpDir(hocrs, transcriptionId, tmpDir, mappingsJson);

        return CompletableFuture.runAsync(() -> {
            try {
                convertHocrToPdf(cacheLocation, eBookFolderPath.toString(), transcriptionId, hash);

                if (titlePage != null) {
                    addTitlePage(titlePage, hash, transcriptionId);
                }
            } catch (Exception e) {
                log.error("Failed to export " + eBookPath, e);
                throw new CompletionException(e);
            }
        }, asyncExecutor);
    }

    private void checkHocrCompatibilityWithTranscription(MultipartFile[] hocrs, long transcriptionId,
                                                         Map<String, String> iiifUrlsMap) {
        Set<String> iiifUrlsSet = iiifUrlsMap.keySet().stream().map(fName -> FilenameUtils.removeExtension(fName))
                .collect(Collectors.toSet());
        Arrays.stream(hocrs).forEach(hocr -> {
            if (hocr.isEmpty()) {
                throw new ConversionException("No provided file content for transcription " + transcriptionId + "!");
            }
            if (hocr.getOriginalFilename() != null && hocr.getOriginalFilename().endsWith(".html")) {
                String filename = FilenameUtils.removeExtension(hocr.getOriginalFilename());

                if (!iiifUrlsSet.contains(filename)) {
                    throw new ConversionException("Cannot convert hocr to pdf without url to image on iiif-server!");
                }
            } else {
                throw new ConversionException("Cannot assign converter for file: " + hocr.getOriginalFilename()
                        + ". File has wrong extension!");
            }
        });
    }

    private void copyHocrFilesToTmpDir(MultipartFile[] hocrs, long transcriptionId, File tmpDir, String mapingsJson)
            throws ConversionException {

        Map<String, String> iiifServerUrlsMap;
        try {
            ObjectMapper mapper = new ObjectMapper();
            iiifServerUrlsMap = mapper.readValue(mapingsJson, Map.class);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ConversionException("Cannot convert json mappings to IIIF server links.");
        }

        checkHocrCompatibilityWithTranscription(hocrs, transcriptionId, iiifServerUrlsMap);

        Arrays.stream(hocrs).forEach(hocrFile -> {

            mkdirs(tmpDir);

            //save hocr
            Path filepath = Paths.get(tmpDir.toString(), hocrFile.getOriginalFilename());
            try (OutputStream os = Files.newOutputStream(filepath)) {
                os.write(hocrFile.getBytes());
            } catch (IOException e) {
                log.error("Cannot write " + hocrFile.getOriginalFilename(), e);
                throw new ConversionException("Cannot copy HOCR file.");
            }

            //and download image form iiif-server
            String imageUrl = iiifServerUrlsMap.get(hocrFile.getOriginalFilename());
            Path target = Paths.get(tmpDir.toString() + "/"
                    + FilenameUtils.removeExtension(hocrFile.getOriginalFilename()) + ".jpg");
            try (InputStream in = new URL(imageUrl).openStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.error("Cannot download " + imageUrl + " to " + target, e);
                throw new ConversionException("Cannot download image file from iiif-server.");
            }
        });
    }

    private void convertHocrToPdf(String tmpDirPath, String ebookLocation, long transcriptionId, String hash) {
        File transcriptionDir = new File(ebookLocation);
        mkdirs(transcriptionDir);
        String hocrConversionScript = scriptsFolder + CONVERT_TO_PDF_SCRIPT_FILE;
        try {
            String commandMessage = commandExecutor
                    .runCommand(Arrays.asList(hocrConversionScript, tmpDirPath, transcriptionDir.getAbsolutePath(),
                            hash + ".pdf")); // pdfFilePath
            if (commandMessage != null && commandMessage.contains("rror")) {
                throw new ConversionException("Cannot build PDF file: " + tmpDirPath + " from transcription: " + transcriptionId);
            }
            log.info("Prepared pdf file for transcriptionId = {}", transcriptionId);

        } catch (IOException e) {
            log.error("Pdf conversion failed for " + tmpDirPath + " (transcription " + transcriptionId + ")", e);
            throw new ConversionException("Conversion script failed");
        }

    }

    private void mkdirs(File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            try {
                Path newDirPath = Paths.get(dir.getPath());
                Files.createDirectories(newDirPath);
            } catch (IOException e) {
                log.error("Can't create dir " + dir, e);
                throw new ConversionException("Can't create directory necessary for PDF generation.");
            }
        }
    }

    private void addTitlePage(String titlePage, String hash, long transcriptionId) {
        try {
            PdfTitlePage pdfTitlePage = new ObjectMapper().readValue(titlePage, PdfTitlePage.class);
            Path eBookPath = EBookLocation.eBookPath(EBookFormat.PDF, transcriptionId, hash);
            Path eBookFullPath = Path.of(rootFolder).resolve(eBookPath);
            Path titlePagePath = eBookFullPath.getParent().resolve(hash + "titlePage.pdf");

            Map<String, Object> data = new HashMap<>();
            data.put("data", pdfTitlePage);

            Context context = new Context();
            context.setVariables(data);

            String htmlContent = templateEngine.process("bad-title-page", context);

            Document document = Jsoup.parse(htmlContent, "UTF-8");
            document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

            try (FileOutputStream fileOutputStream = new FileOutputStream(titlePagePath.toFile())) {
                ITextRenderer renderer = new ITextRenderer();
                SharedContext sharedContext = renderer.getSharedContext();
                sharedContext.setPrint(true);
                sharedContext.setInteractive(false);
                renderer.getFontResolver().addFont(new ClassPathResource("pdf-templates/times.ttf").getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                renderer.setDocumentFromString(document.html());
                renderer.layout();
                renderer.createPDF(fileOutputStream);
            }

            PDFMergerUtility obj = new PDFMergerUtility();
            obj.setDestinationFileName(eBookFullPath.toString());
            obj.addSource(titlePagePath.toFile());
            obj.addSource(eBookFullPath.toFile());
            obj.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

            Files.delete(titlePagePath);

        } catch (IOException e) {
            log.error("Can't create title page for transcription: " + transcriptionId, e);
            throw new ConversionException("Title page creation failed");
        }
    }
}
