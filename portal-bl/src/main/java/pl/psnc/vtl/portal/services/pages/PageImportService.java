package pl.psnc.vtl.portal.services.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.layers.PageLayer.Id;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.repositories.blocks.BlockRepository;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.services.layers.PageLayerService;
import pl.psnc.vtl.portal.services.parsers.TranscriptionContentParser;
import pl.psnc.vtl.portal.services.parsers.ocr.OcrLevel;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;
import pl.psnc.vtl.portal.services.progress.ProgressService;
import pl.psnc.vtl.portal.services.transcriptions.ModificationTrackingService;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionExportService;
import pl.psnc.vtl.portal.services.verses.UndoRedoService;

import javax.transaction.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PageImportService {

    private final PageRepository pageRepository;
    private final VerseRepository verseRepository;
    private final BlockRepository blockRepository;
    
    private final TranscriptionExportService transcriptionExportService;
    private final ModificationTrackingService modificationTrackingService;

    private final List<TranscriptionContentParser> transcriptionContentParsers;

    private final PageLayerService pageLayerService;
    private final ProgressService progressService;
    private final TranscriptionPermissionService transcriptionPermissionService;
    private final UndoRedoService undoRedoService;

    @Value("${vtl.ocr-default-level}")
    private OcrLevel defaultOcrLevel;

    /**
     * Imports content to one page of transcription from plain text or hOCR or TEI file.
     */
    public void addTextLayerToPage(long pageId, String filename, String contentType, InputStream pageTextLayer,
            Long layerId, OcrLevel ocrLevel) throws IOException {
        Page page = pageRepository.getById(pageId);
        transcriptionPermissionService.checkTranscribeAccess(page.getTranscription().getId());

        List<List<Verse>> verseLists = parseOcr(filename, contentType, pageTextLayer, List.of(page));
        if (verseLists.isEmpty() || verseLists.get(0).isEmpty())
            throw new BadRequestException("Could not read any verses from input file", "NoVersesInFile");
        importVersesToPageLayer(verseLists.get(0), page, layerId, ocrLevel);
    }

    private void importVersesToPageLayer(List<Verse> verses, Page page, long layerId, OcrLevel ocrLevel) {
        if (verses.isEmpty())
            return;
        if (OcrLevel.VERSE == (ocrLevel != null ? ocrLevel : defaultOcrLevel))
            verses.forEach(v -> v.getWords().clear());

        replacePageVerses(page, layerId, verses);
        undoRedoService.reset(new Id(page.getId(), layerId));
    }

    public void replacePageVerses(Page page, long layerId, List<Verse> verses) {
        verseRepository.deleteByPageIdAndPageLayerLayerId(page.getId(), layerId);
        PageLayer pageLayer = pageLayerService.getPageLayer(page.getId(), layerId);
        page.setEmpty(verses.isEmpty());
        verses.forEach(v -> {
            v.setPage(page);
            v.setPageLayer(pageLayer);
        });
        verseRepository.saveAll(verses);

        blockRepository.deleteByPageLayerId(pageLayer.getId());

        progressService.resetPageProgress(page.getId());
        transcriptionExportService.resetTranscriptionExportStatus(page.getTranscription().getId());
        modificationTrackingService.trackModification(pageLayer.getId());
    }

    /**
     * Imports content to page layer of transcription from hOCR or TEI file.
     */
    public void addImportedTextLayerToLayer(String filename, String contentType, InputStream pageTextLayer,
            long layerId, long transcriptionId, OcrLevel ocrLevel) throws IOException {
        List<Page> pages = pageRepository.findByTranscriptionIdOrderByPageNo(transcriptionId);
        List<List<Verse>> allVerses = parseOcr(filename, contentType, pageTextLayer, pages);
        for (int i = 0; i < pages.size() && i < allVerses.size(); i++) {
            importVersesToPageLayer(allVerses.get(i), pages.get(i), layerId, ocrLevel);
        }
    }

    private List<List<Verse>> parseOcr(String filename, String contentType, InputStream pageTextLayer, List<Page> pages)
            throws IOException {
        TranscriptionPageContentType pageContentType = PageContentTypeRecognizer.recognize(filename, contentType);
        TranscriptionContentParser transcriptionPageParser = transcriptionContentParsers.stream()
                .filter(parser -> parser.support(pageContentType)).findFirst().orElseThrow(() -> new IOException(
                        "TranscriptionPageParser for content type: " + pageContentType + " not found."));
        return transcriptionPageParser.parseVerses(pageTextLayer, pages);
    }
}
