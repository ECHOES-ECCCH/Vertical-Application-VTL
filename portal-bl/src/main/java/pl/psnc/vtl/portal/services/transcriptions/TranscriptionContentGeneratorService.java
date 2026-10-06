package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.controllers.transcriptions.TranscriptionGenerationProperties;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionRepository;
import pl.psnc.vtl.portal.services.imports.BeanLoader;
import pl.psnc.vtl.portal.services.layers.LayerService;
import pl.psnc.vtl.portal.services.pages.PageHelper;
import pl.psnc.vtl.portal.services.pages.PageService;
import pl.psnc.vtl.portal.services.transcriptions.content.TranscriptionContentGeneratorFactory;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class TranscriptionContentGeneratorService {

    private final TranscriptionContentGeneratorFactory transcriptionContentGeneratorFactory;

    private final TranscriptionRepository transcriptionRepository;

    public void prepareTranscriptionContent(TranscriptionGenerationProperties props, OutputStream outputStream) {

        Transcription transcription = transcriptionRepository.findById(props.getTranscriptionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Unable to find transcription with id " + props.getTranscriptionId()));

        if (props.getLayerId() == null) {
            prepareZipLayersFile(props, outputStream, transcription);
        } else {
            prepareZipPagesFile(props, outputStream);
        }
    }


    public String prepareSingleFile(TranscriptionPageContentType type, Transcription transcription, Layer layer) {
        log.info("Preparing single file with multiple pages for transcription: {}", transcription.getId());
        if (transcription.getTeiHeader() != null)
            transcription.setToSingleFileExport(true);
        return transcriptionContentGeneratorFactory.provideGenerator(type).generate(transcription, layer);
    }


    public static String prepareLayerName(Layer layer, TranscriptionPageContentType type) {
        return FilenameUtils.removeExtension(layer.getName()) + "." + type.getExtensions().get(0);

    }

    private void prepareZipLayersFile(TranscriptionGenerationProperties props,
            OutputStream outputStream, Transcription transcription) {

        log.info("Preparing zip file for transcription: {}", transcription.getId());
        var layerService = BeanLoader.getBean(LayerService.class);
        List<Layer> layers = layerService.getAllLayersPerTranscription(props.getTranscriptionId());
        try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {

            for (Layer layer : layers) {
                String generatedContent = transcriptionContentGeneratorFactory.provideGenerator(
                        props.getContentType()).generate(transcription, layer);
                String name = TranscriptionContentGeneratorService.prepareLayerName(layer,
                        props.getContentType());
                final ZipEntry zipEntry = new ZipEntry(name);
                zipOut.putNextEntry(zipEntry);
                zipOut.write(generatedContent.getBytes());
            }
//            List<Page> pagesToBeUsedForGeneration = pageRepository.findByTranscriptionId(transcription.getId());
//            for (final Page page : pagesToBeUsedForGeneration) {
//                page.setVerses(verseRepository.findByPageOrderByVerseNoAsc(page));
//                String generatedContent = contentGenerator.generate(page, true);
//                final ZipEntry zipEntry = new ZipEntry(
//                        PageHelper.preparePageName(page, transcriptionGenerationProperties.getContentType()));
//                zipOut.putNextEntry(zipEntry);
//                zipOut.write(generatedContent.getBytes());
//            }
        } catch (final IOException e) {
            log.error("Unable to properly transfer exported file to client for transcription {}",
                    transcription.getId());
        }
    }

    public List<Pair<String, String>> prepareLayerContents(TranscriptionGenerationProperties props) {
        var pageService = BeanLoader.getBean(PageService.class);
        List<Page> pages = pageService.getPages(props.getTranscriptionId());
        List<Pair<String, String>> result = new ArrayList<>();
        for (Page page : pages) {
            for (var pair : pageService.getPageContent(page.getId(), props.getContentType(), props.getLayerId())) {
                String pageName = PageHelper.preparePageName(page, props.getContentType(), pair.getLeft());
                result.add(new ImmutablePair<>(pageName, pair.right));
            }
        }
        return result;
    }

    private void prepareZipPagesFile(TranscriptionGenerationProperties props, OutputStream outputStream) {
        log.info("Preparing zip file for transcription: {}", props.getTranscriptionId());
        try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
            List<Pair<String, String>> contents = prepareLayerContents(props);
            for (var pair : contents) {
                var zipEntry = new ZipEntry(pair.getLeft());
                zipOut.putNextEntry(zipEntry);
                zipOut.write(pair.getRight().getBytes());
            }
        } catch (IOException e) {
            log.error("Unable to properly transfer exported file to client for transcription {}",
                    props.getTranscriptionId(), e);
        }
    }
}
