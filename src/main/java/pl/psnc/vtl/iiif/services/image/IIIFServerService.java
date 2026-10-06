package pl.psnc.vtl.iiif.services.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.iiif.converters.ConversionException;
import pl.psnc.vtl.iiif.converters.Converter;
import pl.psnc.vtl.iiif.converters.ConverterFactory;
import pl.psnc.vtl.iiif.converters.VipsConverter;
import pl.psnc.vtl.iiif.model.DownloadFilesResponse;
import pl.psnc.vtl.iiif.model.RemoveFileResponse;
import pl.psnc.vtl.iiif.utils.ConversionSettings;
import pl.psnc.vtl.iiif.utils.FileDownloader;
import pl.psnc.vtl.iiif.utils.StoragePathUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IIIFServerService {

    @Value("${vtl.iiif-tools.storage}")
    private String rootFolder;

    @Value("${vtl.iiif-tools.filename-length-limit}")
    private Integer filenameLengthLimit;

    private final ConverterFactory converterFactory;

    private final StoragePathUtils storagePathUtils;

    private final FileDownloader fileDownloader;

    private volatile int taskCounter = 0;

    private synchronized void startTask() {
        taskCounter++;
    }

    private synchronized void finishTask() {
        taskCounter--;
    }

    public boolean isBusy() {

        return taskCounter > 0;
    }

    public int taskCounter() {
        return taskCounter;
    }

    public void saveImage(long transcriptionId, InputStream inputStream, String filename) throws IOException {
        String targetFilePath = storagePathUtils.getOriginalFilepath(transcriptionId, filename);
        FileUtils.copyInputStreamToFile(inputStream, new File(targetFilePath));

        String extension = FilenameUtils.getExtension(filename);
        if (extension != null && (extension.equals("djvu") || extension.equals("djv"))) {
            ((VipsConverter) converterFactory.getConverter(filename)).convertDjvuToPdf(targetFilePath);
        }
    }


    public List<String> makePyramid(long transcriptionId, String filename) throws ConversionException {
        try {
            startTask();
            String sourceFilePath = storagePathUtils.getOriginalFilepath(transcriptionId, filename);
            String targetFilePath = storagePathUtils.getPyramidFilepath(transcriptionId, filename);

            ConversionSettings conversionSettings = ConversionSettings.builder().withSourceFilePath(sourceFilePath)
                    .withTargetFilePath(targetFilePath).withTranscriptionId(transcriptionId).build();

            Converter converter = converterFactory.getConverter(filename);
            return converter.convert(conversionSettings);
        } finally {
            finishTask();
        }
    }


    public RemoveFileResponse deleteTranscriptionImages(long transcriptionId) throws IOException {
        long freedQuota = FileUtils.sizeOfDirectory(new File(storagePathUtils.getOriginalDirectory(transcriptionId)));
        FileUtils.deleteDirectory(new File(storagePathUtils.getOriginalDirectory(transcriptionId)));
        FileUtils.deleteDirectory(new File(storagePathUtils.getPyramidDirectory(transcriptionId)));
        FileUtils.deleteDirectory(new File(storagePathUtils.getEbookDirectory(transcriptionId)));
        return new RemoveFileResponse(freedQuota);
    }


    public void deleteAllTranscriptionsImages() throws IOException {
        FileUtils.cleanDirectory(new File(storagePathUtils.getOriginalRootDirectory()));
        File[] files = new File(storagePathUtils.getPyramidRootDirectory()).listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.getName().equals("iipsrv.fcgi")) {
                    FileUtils.deleteDirectory(file);
                }
            }
        }
        FileUtils.cleanDirectory(new File(storagePathUtils.getEbookRootDirectory()));
    }


    public DownloadFilesResponse downloadImages(long transcriptionId, List<String> urls) throws IOException,
            NameCannotBeExtractedException {
        try {
            startTask();
            log.debug("URLs do download for transcription " + transcriptionId + ": " + urls);
            FilenamesExtractor extractor = new FilenamesExtractor(urls);
            List<String> filenames = extractor.getFilenames().stream()
                    .map(f ->  determineFilename(transcriptionId, f))
                    .collect(Collectors.toList());
            Map<String, String> urlsToFilenames = new LinkedHashMap<>();
            List<File> filesToCleanup = new ArrayList<>();
            DownloadFilesResponse downloadFilesResponse = new DownloadFilesResponse();
            long quota = 0;

            for (int i = 0; i < urls.size(); i++) {
                urlsToFilenames.put(urls.get(i), filenames.get(i));
                File targetFilePath = new File(storagePathUtils.getOriginalFilepath(transcriptionId, filenames.get(i)));
                filesToCleanup.add(targetFilePath);

                try {
                    Instant inst1 = Instant.now();
                    fileDownloader.downloadFile(new URL(urls.get(i)), targetFilePath);
                    Instant inst2 = Instant.now();
                    log.debug("Download of {} took {} s", urls.get(i), Duration.between(inst1, inst2).toSeconds());
                    quota += targetFilePath.length();
                } catch (IOException e) {
                    tryCleanAfterException(filesToCleanup, e);
                    throw e;
                }
            }
            downloadFilesResponse.setQuota(quota);
            downloadFilesResponse.setUrlsToFilenames(urlsToFilenames);
            return downloadFilesResponse;
        } finally {
            finishTask();
        }
    }

    private String determineFilename(long transcriptionId, String filename) {
        if (filename.length() > filenameLengthLimit) {
            String extension = "." + FilenameUtils.getExtension(filename);
            filename = filename.substring(0, filenameLengthLimit - extension.length()) + extension;
        }

        if (Files.notExists(Paths.get(storagePathUtils.getOriginalFilepath(transcriptionId, filename)))) {
            return filename;
        }

        int index = 1;

        while (Files.exists(Paths.get(storagePathUtils.getOriginalFilepath(transcriptionId,
                createFilename(filename, index))))) {
            index++;
        }

        return createFilename(filename, index);
    }

    private String createFilename(String filename, int index) {
        return index + "-" + filename;
    }

    private void tryCleanAfterException(List<File> filesToCleanup, Exception reason) {
        try {
            for (File file : filesToCleanup) {
                FileUtils.forceDelete(file);
            }
        } catch (IOException e) {
            reason.addSuppressed(e);
        }
    }

    public RemoveFileResponse deleteImage(long transcriptionId, String filename) throws IOException {
        log.debug("File to delete - " + storagePathUtils.getOriginalFilepath(transcriptionId, filename));
        long freedQuota = FileUtils.sizeOf(new File(storagePathUtils.getOriginalFilepath(transcriptionId, filename)));
        FileUtils.forceDelete(new File(storagePathUtils.getOriginalFilepath(transcriptionId, filename)));

        String extension = FilenameUtils.getExtension(filename);
        if (extension != null && (extension.equals("djvu") || extension.equals("djv"))) {
            filename = FilenameUtils.removeExtension(filename) + ".pdf";
            log.debug("Additional file to delete - " + storagePathUtils.getOriginalFilepath(transcriptionId, filename));
            FileUtils.forceDelete(new File(storagePathUtils.getOriginalFilepath(transcriptionId, filename)));
        }

        return new RemoveFileResponse(freedQuota);
    }

    public void deletePyramid(long transcriptionId, String filename) throws IOException {
        log.debug("File to delete - " + storagePathUtils.getPyramidFilepath(transcriptionId, filename));
        FileUtils.forceDelete(new File(storagePathUtils.getPyramidFilepath(transcriptionId, filename)));
    }
}
