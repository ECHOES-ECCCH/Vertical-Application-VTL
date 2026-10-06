package pl.psnc.vtl.portal.services.pages;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.conf.logging.LogAction;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.dto.permissions.ViewMode;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.layers.PageLayer.Id;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.pages.projections.PageIdAndPageNo;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.verses.OcrMode;
import pl.psnc.vtl.portal.model.verses.StudentVerse;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.repositories.iiifserver.IIIFServerRepository;
import pl.psnc.vtl.portal.repositories.iiiftools.IIIFToolsRepository;
import pl.psnc.vtl.portal.repositories.iiiftools.RemoveFileResponse;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.services.blocks.BlockService;
import pl.psnc.vtl.portal.services.iiifserver.IIIFUriResolver;
import pl.psnc.vtl.portal.services.imports.Link;
import pl.psnc.vtl.portal.services.layers.PageLayerService;
import pl.psnc.vtl.portal.services.pages.content.TranscriptionPageContentGeneratorFactory;
import pl.psnc.vtl.portal.services.parsers.ocr.OCRToolsService;
import pl.psnc.vtl.portal.services.parsers.ocr.OcrLevel;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;
import pl.psnc.vtl.portal.services.progress.ProgressService;
import pl.psnc.vtl.portal.services.transcriptions.ModificationTrackingService;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionExportService;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionHelper;
import pl.psnc.vtl.portal.services.users.UserService;
import pl.psnc.vtl.portal.services.verses.StudentVerseService;
import pl.psnc.vtl.portal.services.verses.UndoRedoService;
import pl.psnc.vtl.portal.services.verses.VerseService;
import pl.psnc.vtl.portal.utils.IIIFUtils;

import javax.persistence.EntityNotFoundException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PageService {

    private final PageRepository pageRepository;

    private final ProgressService progressService;

    private final PageImportService pageImportService;

    private final OCRToolsService ocrToolsService;

    private final ModificationTrackingService modificationTrackingService;

    private final VerseService verseService;

    private final StudentVerseService studentVerseService;

    private final VerseRepository verseRepository;

    private final TranscriptionPermissionService transcriptionPermissionService;

    private final IIIFToolsRepository iiifToolsRepository;

    private final IIIFUriResolver iiifUriResolver;

    private final IIIFServerRepository iiifServerRepository;

    private final TranscriptionPageContentGeneratorFactory transcriptionPageContentGeneratorFactory;

    private final PageLayerService pageLayerService;
    private final BlockService blockService;
    private final UndoRedoService undoRedoService;

    @Value("${vtl.ocr-default-level}")
    private OcrLevel defaultOcrLevel;

    private final TranscriptionExportService transcriptionExportService;

    private final UserService userService;

    @Transactional(readOnly = true)
    public Page getPage(long transcriptionId, long pageId, ViewMode viewMode) {
        Page page = pageRepository.findByTranscriptionIdAndId(transcriptionId, pageId).orElseThrow(
                () -> new EntityNotFoundException(
                        "Unable to find " + Page.class.getName() + " with id '" + pageId + "!"));
        if (viewMode == null) {
            viewMode = transcriptionPermissionService.checkReadAccess(page.getTranscription().getId());
        }
        page.setViewMode(viewMode);
        completePage(page);
        return page;
    }

    @Transactional(readOnly = true)
    public Page getPageWithBlocks(long transcriptionId, long pageId, ViewMode viewMode) {
        Page page = getPage(transcriptionId, pageId, viewMode);
        List<Verse> verses = verseService.completeVerses(page);

        for (PageLayer pageLayer : page.getPageLayers()) {
            List<Object> blocks = blockService.getBlocks(pageLayer, verses);
            pageLayer.setBlocks(blocks);
        }

        return page;
    }

    @Transactional(readOnly = true)
    public Page completePage(Page page) {
        page.setTitleLabel(TranscriptionHelper.findTitleLabel(page.getTranscription()));
        Page prevPage = pageRepository.findByTranscriptionIdAndPageNo(page.getTranscription().getId(),
                page.getPageNo() - 1);
        if (prevPage != null) {
            page.setPrevPageId(prevPage.getId());
        }
        Page nextPage = pageRepository.findByTranscriptionIdAndPageNo(page.getTranscription().getId(),
                page.getPageNo() + 1);
        if (nextPage != null) {
            page.setNextPageId(nextPage.getId());
        }
        List<Verse> verses = verseService.completeVerses(page);
        page.setVerses(verses);

        if (ViewMode.STUDENT.equals(page.getViewMode())) {
            List<StudentVerse> studentVerses = studentVerseService.completeVerses(verses);
            page.setStudentVerses(studentVerses);
        }
        return page;
    }

    public void createPages(Transcription transcription, Map<String, List<String>> fileToPyramidNames) {
        List<String> filenames = fileToPyramidNames.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty()).map(Map.Entry::getKey)
                .collect(Collectors.toCollection(ArrayList::new));
        var nameComparator = new ReversePageFilenameComparator();
        filenames.sort(nameComparator);

        List<Page> pages = pageRepository.findByTranscriptionIdOrderByPageNo(transcription.getId());

        Integer firstMovedIdx;
        int endNumber = pages.size() - 1;

        for (int i = 0; i < filenames.size(); i++) {
            String fileName = filenames.get(i);
            List<String> pyramidsFilesNames = fileToPyramidNames.get(fileName);
            int pagesCount = pyramidsFilesNames.size();

            for (int j = 0; j < pagesCount; j++) {
                String urlEncodedPyramidFileName = URLEncoder.encode(pyramidsFilesNames.get(j), StandardCharsets.UTF_8);

                Page page = new Page();
                page.setTranscription(transcription);
                String iiifUrl = transcription.getId() + "/" + urlEncodedPyramidFileName + "/info.json";
                page.setIiifUrl(iiifUrl);

                Pair<Integer, Integer> result = getPageNumberFromFilename(fileName, pages,
                        nameComparator, endNumber);
                int pageNo = result.getLeft() + filenames.size() - i - 1;
                firstMovedIdx = result.getRight();
                if (j == 0 && firstMovedIdx != null) {
                    increasePageNumber(pages, firstMovedIdx, endNumber, filenames.size() - i + pagesCount - 1);
                }
                if (firstMovedIdx != null) {
                    endNumber = firstMovedIdx - 1;
                } else {
                    endNumber = endNumber - 1;
                }

                if (pagesCount == 1) {
                    page.setRelativeNo(null);
                } else {
                    page.setRelativeNo(j + 1);
                }
                page.setPageNo(pageNo + j);
                page.setPath(fileName);

                BoundingBox boundingBox = IIIFUtils.getBoundingBoxFromInfoJson(
                        iiifServerRepository.iiifServerInfoJsonProvider(iiifUriResolver.getInternalInfoJsonUri(page)));
                page.setBoundingBox(boundingBox);
                page.setThumbnailUrl(iiifUriResolver.getThumbnailUrl(
                        iiifUriResolver.createPublicImageUrl(transcription.getId(), urlEncodedPyramidFileName),
                        boundingBox.getLowerRightX(), boundingBox.getLowerRightY(), true));
                page.setPercentOfTranscribed(0);
                page.setPercentOfVerified(0);
                pageRepository.saveAndFlush(page);

                pageLayerService.createPageLayersForTranscription(page.getId(), transcription.getId());
            }
        }
        // page numbering is sometimes buggy, but too complicated to fix,
        // so just check and fix consistency now
        checkPageNumbersConsistency(transcription);

        progressService.updateTranscriptionProgress(transcription.getId());
        transcriptionExportService.resetTranscriptionExportStatus(transcription.getId());
    }

    public void createPagesFromManifest(Transcription transcription, List<Link> publicationLinks,
            Map<String, String> urlsToFileNames) {

        HashMap<String, Link> filenameToLink = new HashMap<>();
        for (Link link : publicationLinks) {
            filenameToLink.put(urlsToFileNames.get(link.getUrl()), link);
        }

        List<String> filenames = new ArrayList<>(urlsToFileNames.values());
        var nameComparator = new ReversePageFilenameComparator();
        filenames.sort(nameComparator);

        List<Page> pages = pageRepository.findByTranscriptionIdOrderByPageNo(transcription.getId());

        Integer firstMovedIdx;
        int endNumber = pages.size() - 1;

        for (int i = 0; i < filenames.size(); i++) {
            String fileName = filenames.get(i);
            Link link = filenameToLink.get(fileName);

            Page page = new Page();
            page.setTranscription(transcription);
            page.setIiifUrl(link.getInfoJsonUrl());

            Pair<Integer, Integer> result = getPageNumberFromFilename(fileName, pages, nameComparator, endNumber);
            int pageNo = result.getLeft() + filenames.size() - i - 1;
            firstMovedIdx = result.getRight();
            if (firstMovedIdx != null) {
                increasePageNumber(pages, firstMovedIdx, endNumber, filenames.size() - i);
            }
            if (firstMovedIdx != null) {
                endNumber = firstMovedIdx - 1;
            } else {
                endNumber = endNumber - 1;
            }

            page.setRelativeNo(null);
            page.setPageNo(pageNo);
            page.setPath(fileName);

            page.setBoundingBox(new BoundingBox(0, 0, link.getWidth(), link.getHeight()));
            page.setThumbnailUrl(link.getThumbnailUrl());
            page.setPercentOfTranscribed(0);
            page.setPercentOfVerified(0);
            pageRepository.saveAndFlush(page);

            pageLayerService.createPageLayersForTranscription(page.getId(), transcription.getId());
        }
        // page numbering is sometimes buggy, but too complicated to fix,
        // so just check and fix consistency now
        checkPageNumbersConsistency(transcription);

        progressService.updateTranscriptionProgress(transcription.getId());
        transcriptionExportService.resetTranscriptionExportStatus(transcription.getId());
    }

    private void checkPageNumbersConsistency(Transcription transcription) {
        List<Page> pages = pageRepository.findByTranscriptionIdOrderByPageNo(transcription.getId());
        int pageNo = 1;
        for (Page page : pages) {
            if (page.getPageNo() != pageNo) {
                page.setPageNo(pageNo);
                pageRepository.save(page);
            }
            pageNo++;
        }
    }

    @Transactional(readOnly = true)
    @SneakyThrows
    public List<ImmutablePair<Layer, String>> getPageContent(long pageId,
            TranscriptionPageContentType transcriptionPageContentType, Long layerId) {
        List<Layer> layers = layerId == null ? pageLayerService.getPageLayers(pageId)
                : pageLayerService.getPagerLayer(pageId, layerId);
        Page page = pageRepository.getById(pageId);

        return transcriptionPageContentGeneratorFactory.provideGenerator(transcriptionPageContentType)
                .generate(page, layers);
    }

    @Transactional(readOnly = true)
    public void writeZippedContent(List<ImmutablePair<Layer, String>> contents, long pageId, TranscriptionPageContentType transcriptionPageContentType, OutputStream outputStream) {
        try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
            Page page = pageRepository.getById(pageId);
            for (var content : contents) {
                String fname = PageHelper.preparePageName(page, transcriptionPageContentType, content.left);
                final ZipEntry zipEntry = new ZipEntry(fname);
                zipOut.putNextEntry(zipEntry);
                zipOut.write(content.right.getBytes());
            }
        } catch (final IOException e) {
            log.error("Unable to properly export page {}", pageId);
        }
    }

    public void recognizePage(Page page, String lang, OcrMode ocrMode, long layerId, OcrLevel ocrLevel,
            boolean handleColumns) {

        page.getTranscription().setLastSetLanguageCode(lang.replaceFirst("([a-z_]+).*", "$1"));

        boolean includeWords = OcrLevel.WORD == Objects.requireNonNullElse(ocrLevel, defaultOcrLevel);
        if (ocrMode == OcrMode.UPDATE) {
            ocrToolsService.recognizeEmptyVerses(page, layerId, lang);
        } else {
            List<Verse> versesToSave = ocrToolsService.recognizePage(page, lang, includeWords, handleColumns);
            pageImportService.replacePageVerses(page, layerId, versesToSave);
        }

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Recognized text using OCR on Page with id - {} and Layer with id - {} in Transcription with id - {}",
                page.getId(), layerId, page.getTranscription().getId());
        LogAction.clearMDCAction();
    }

    public PageLayer.Id recognizePage(long transcriptionId, long pageId, String lang, OcrMode ocrMode,
            long layerId, OcrLevel ocrLevel, boolean handleColumns) {
        Page page =
                getPage(transcriptionId, pageId, transcriptionPermissionService.checkTranscribeAccess(transcriptionId));
        undoRedoService.reset(new Id(pageId, layerId));
        recognizePage(page, lang, ocrMode, layerId, ocrLevel, handleColumns);
        return pageLayerService.getPageLayer(pageId, layerId).getId();
    }

    public List<Page> increasePageNumber(List<Page> pages, int startNumber, int endNumber, int counter) {
        for (int i = startNumber; i <= endNumber; i++) {
            int currentPageNo = pages.get(i).getPageNo();
            pages.get(i).setPageNo(currentPageNo + counter);
            pageRepository.saveAndFlush(pages.get(i));
        }
        return pages;
    }


    /**
     * Gets the pageNo for a given filename in the current list of pages (according to the given reverse comparator). For performance issues it finds this pageNo from tha last element moved in the previous loop).
     *
     * @param filename                      filename for which "pageNo" should be found
     * @param pages                         list of current pages
     * @param reversePageFilenameComparator reverse comparator
     * @param lastElement                   index of the last element from which searching starts
     * @return pair pageNo, index of first exiting page to be moved down
     */
    public Pair<Integer, Integer> getPageNumberFromFilename(String filename, List<Page> pages,
            ReversePageFilenameComparator reversePageFilenameComparator, int lastElement) {
        int pageNo;
        Integer firstIndexToMove;

        int numberFromFilename = reversePageFilenameComparator.getNumberFromFilename(filename);
        String suffixFromFilename = reversePageFilenameComparator.getSuffixFromFilename(filename);

        for (int i = lastElement; i >= 0; i--) {
            Page currentPage = pages.get(i);

            int numberFromCurrentPage = reversePageFilenameComparator.getNumberFromFilename(pages.get(i).getPath());
            String suffixFromCurrentPage = reversePageFilenameComparator.getSuffixFromFilename(pages.get(i).getPath());

            if (numberFromCurrentPage < numberFromFilename) {
                pageNo = currentPage.getPageNo() + 1;
                firstIndexToMove = i + 1;
                return new ImmutablePair<>(pageNo, firstIndexToMove);
            }
            if (numberFromCurrentPage == numberFromFilename) {

                boolean insertBetween = isInsertBetween(pages, reversePageFilenameComparator, suffixFromFilename, i,
                        numberFromCurrentPage);

                if (suffixFromFilename != null && suffixFromCurrentPage != null) {
                    if (suffixFromCurrentPage.compareTo(suffixFromFilename) > 0 && !insertBetween) {
                        pageNo = currentPage.getPageNo();
                        firstIndexToMove = i;
                        return new ImmutablePair<>(pageNo, firstIndexToMove);
                    } else {
                        if (!insertBetween) {
                            pageNo = currentPage.getPageNo() + 1;
                            firstIndexToMove = i + 1;
                            return new ImmutablePair<>(pageNo, firstIndexToMove);
                        }
                    }
                } else if (suffixFromCurrentPage == null && suffixFromFilename != null) {
                    pageNo = currentPage.getPageNo() + 1;
                    firstIndexToMove = i + 1;
                    return new ImmutablePair<>(pageNo, firstIndexToMove);
                } else {
                    if (suffixFromCurrentPage == null && !insertBetween) {
                        pageNo = currentPage.getPageNo() + 1;
                        if (i != 0) {
                            firstIndexToMove = i - 1;
                        } else {
                            firstIndexToMove = null;
                        }
                        return new ImmutablePair<>(pageNo, firstIndexToMove);
                    } else {
                        if (!insertBetween) {
                            pageNo = currentPage.getPageNo();
                            firstIndexToMove = i;
                            return new ImmutablePair<>(pageNo, firstIndexToMove);
                        }
                    }
                }
            }
        }
        return new ImmutablePair<>(1, 0);
    }


    private boolean isInsertBetween(List<Page> pagesList, ReversePageFilenameComparator reversePageFilenameComparator,
            String suffixFromFilename, int i, int numberFromCurrentPage) {
        if (i > 0) {
            int prevNumber = reversePageFilenameComparator.getNumberFromFilename(pagesList.get(i - 1).getPath());
            if (prevNumber == numberFromCurrentPage) {
                String prevSuffix = reversePageFilenameComparator.getSuffixFromFilename(pagesList.get(i - 1).getPath());
                if (prevSuffix == null) {
                    return true;
                } else {
                    if (suffixFromFilename == null) {
                        return true;
                    }
                    return prevSuffix.compareTo(suffixFromFilename) > 0;
                }
            }
        }
        return false;
    }


    public void deletePage(long pageId) {
        Page page = pageRepository.findById(pageId).orElseThrow(EntityNotFoundException::new);
        Transcription transcription = page.getTranscription();
        transcriptionPermissionService.checkAdminAccess(transcription.getId());

        boolean sharedPage =
                pageRepository.findByTranscriptionIdAndPath(transcription.getId(), page.getPath()).size() > 1;

        if (!sharedPage && page.getPath() != null) {
            RemoveFileResponse  response = iiifToolsRepository.deletePageImage(transcription.getId(), page.getPath());
            if (response != null) {
                userService.updateUsedSpaceQuota(transcription.getCreator(), -response.getQuota());
            }
        }

        String pyramid = iiifUriResolver.getLocalPyramidFilename(page);
        if (pyramid != null)
            iiifToolsRepository.deletePagePyramid(transcription.getId(), pyramid);

        reNumerateOtherPages(page);
        pageRepository.deleteById(page.getId());

        modificationTrackingService.pageRemoved(page);

        transcriptionExportService.resetTranscriptionExportStatus(transcription.getId());

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Removed Page with id - {}.", pageId);
        LogAction.clearMDCAction();
    }

    private void reNumerateOtherPages(Page page) {
        List<Page> pagesToBeReNumerated = pageRepository.findByTranscriptionIdWithNumbersGreaterThan(
                page.getTranscription().getId(), page.getPageNo());
        pagesToBeReNumerated.forEach(p -> p.setPageNo(p.getPageNo() - 1));
    }

    public void markPageAsEmpty(long pageId, boolean empty) {
        Page page = pageRepository.findById(pageId).orElseThrow(EntityNotFoundException::new);
        Transcription transcription = page.getTranscription();
        transcriptionPermissionService.checkTranscribeAccess(transcription.getId());
        List<Verse> verses = verseRepository.findByPage(page);
        if (!verses.isEmpty() && empty) {
            verseService.deleteVerses(verses.stream().map(Verse::getId).collect(toList()));
        }

        page.setEmpty(empty);
        page.setPercentOfTranscribed(100);
        page.setPercentOfVerified(100);
        pageRepository.save(page);
        page.getPageLayers().forEach(pl -> {
            pl.setPercentOfTranscribed(100);
            pl.setPercentOfVerified(100);
            undoRedoService.reset(pl.getId());
        });
        progressService.updateTranscriptionProgress(pageId);

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Marked Page with id - {} as empty.", pageId);
        LogAction.clearMDCAction();
    }

    public void setTeiPBForPage(long pageId, String newPBValue) {
        Page page = pageRepository.findById(pageId).orElseThrow(EntityNotFoundException::new);

        transcriptionPermissionService.checkTranscribeAccess(page.getTranscription().getId());
        page.setTeiPbValue(newPBValue);
    }

    public List<PageIdAndPageNo> getPagesIdsAndPageNums(long transcriptionId) {
        transcriptionPermissionService.checkReadAccess(transcriptionId);
        return pageRepository.findByTranscriptionIdOrderByPageNo(transcriptionId, PageIdAndPageNo.class);
    }

    public List<Page> getPages(long transcriptionId) {
        transcriptionPermissionService.checkReadAccess(transcriptionId);
        return pageRepository.findByTranscriptionIdOrderByPageNo(transcriptionId);
    }
}
