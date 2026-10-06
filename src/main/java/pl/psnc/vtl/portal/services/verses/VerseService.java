package pl.psnc.vtl.portal.services.verses;

import com.google.common.util.concurrent.Striped;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import pl.psnc.vtl.portal.conf.logging.LogAction;
import pl.psnc.vtl.portal.controllers.verses.VerseCreationDto;
import pl.psnc.vtl.portal.dto.permissions.ViewMode;
import pl.psnc.vtl.portal.dto.regions.BoundingBoxCore;
import pl.psnc.vtl.portal.dto.verses.VersesAndBlocksDto;
import pl.psnc.vtl.portal.model.events.Event;
import pl.psnc.vtl.portal.model.indexes.Index;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.verses.*;
import pl.psnc.vtl.portal.model.words.Word;
import pl.psnc.vtl.portal.repositories.indexes.IndexRepository;
import pl.psnc.vtl.portal.repositories.pages.PageLayersRepository;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.repositories.verses.CommentRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseContentRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.repositories.words.WordRepository;
import pl.psnc.vtl.portal.services.blocks.BlockService;
import pl.psnc.vtl.portal.services.events.EventParticipationService;
import pl.psnc.vtl.portal.services.imports.BeanLoader;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;
import pl.psnc.vtl.portal.services.progress.ProgressService;
import pl.psnc.vtl.portal.services.properties.PropertyService;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;
import pl.psnc.vtl.portal.services.transcriptions.ModificationTrackingService;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionExportService;
import pl.psnc.vtl.portal.services.verses.UndoRedoService.Operation;
import pl.psnc.vtl.portal.utils.BoundingBoxUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.validation.ValidationException;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class VerseService {
    private final LoggedInUserHolder loggedInUserHolder;

    private final VerseRepository verseRepository;

    private final VerseContentRepository verseContentRepository;

    private final CommentRepository commentRepository;

    private final PageRepository pageRepository;

    private final TranscriptionPermissionService transcriptionPermissionService;

    private final ModificationTrackingService modificationTrackingService;

    private final BlockService blockService;

    private final PropertyService propertyService;

    private final ProgressService progressService;

    private final PageLayersRepository pageLayersRepository;
    private final IndexRepository indexRepository;
    private final WordRepository wordRepository;

    private final UndoRedoService undoRedoService;

    private final TranscriptionExportService transcriptionExportService;

    private final EventParticipationService eventParticipationService;

    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    private static final Striped<Lock> pageStatusLock = Striped.lazyWeakLock(64);


    public static void setStatus(List<Verse> verses, ViewMode viewMode, VerseStatus status, User user) {
        for (Verse v : verses) {
            status = checkNextStatus(v, viewMode, status);
            if (v.getStatus() == status)
                continue;
            v.setStatus(status);
            if (user != null && v.getCurrentVerseContent() != null) {
                if (status == VerseStatus.INCORRECT) {
                    v.getCurrentVerseContent().setRejectedBy(user);
                } else {
                    v.getCurrentVerseContent().setRejectedBy(null);
                }
            }
            v.setAuthorOfLastStatusModification(user);
        }
    }

    public Verse getVerseToMakeAction(Long verseId) {
        Verse verse = verseRepository.findById(verseId)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find verse with id: '" + verseId + "'!"));

        if (!eventParticipationService.isPageAccessible(verse.getPage().getId()))
            transcriptionPermissionService.checkTranscribeAccess(verse.getPage().getTranscription().getId());

        modificationTrackingService.trackModification(verse.getPageLayer().getId());
        return verse;
    }

    @Transactional(readOnly = true)
    public List<Verse> completeVerses(PageLayer pageLayer) {
        List<Verse> verses = verseRepository.findByPageLayerOrderByVerseNoAsc(pageLayer);
        List<Word> allWords = wordRepository.findByVersePageLayer(pageLayer);
        return completeVerses(verses, allWords);
    }

    @Transactional(readOnly = true)
    public List<Verse> completeVerses(Page page) {
        List<Verse> verses = verseRepository.findByPageOrderByVerseNoAsc(page);
        List<Word> allWords = wordRepository.findByVersePageId(page.getId());
        return completeVerses(verses, allWords);
    }

    private List<Verse> completeVerses(List<Verse> verses, List<Word> allWords) {
        Map<Long, Integer> commentCounts = commentRepository.getVerseCommentCounts(verses).stream()
                .collect(Collectors.toMap(tuple -> tuple[0], tuple -> tuple[1].intValue()));
        Map<Long, Set<Index>> indexesByVerse = indexRepository.findAllByVerse(verses).stream()
                .collect(Collectors.groupingBy(
                        tuple -> (Long) tuple[0],
                        Collectors.mapping(tuple -> (Index) tuple[1], Collectors.toSet())));
        Map<Long, List<Word>> wordsByVerse = allWords.stream().collect(groupingBy(w -> w.getVerse().getId()));
        Map<Long, Set<Index>> indexesByWord = indexRepository.findAllByWord(allWords).stream()
                .collect(Collectors.groupingBy(
                        tuple -> (Long) tuple[0],
                        Collectors.mapping(tuple -> (Index) tuple[1], Collectors.toSet())));

        allWords.forEach(entityManager::detach);
        verses.forEach(entityManager::detach);
        for (Verse verse : verses) {
            verse.setComments(commentCounts.getOrDefault(verse.getId(), 0));
            verse.setIndexes(indexesByVerse.getOrDefault(verse.getId(), Set.of()));
            if (wordsByVerse.containsKey(verse.getId())) {
                List<Word> words = wordsByVerse.get(verse.getId());
                Collections.sort(words);
                verse.setWords(words);
            } else {
                verse.setWords(List.of());
            }
        }
        for (Word word : allWords) {
            word.setIndexes(indexesByWord.getOrDefault(word.getId(), Set.of()));
        }
        return verses;
    }

    public PageLayer.Id addVerse(Long pageId, VerseCreationDto verseCreationDto, Long layerId) {
        PageLayer pageLayer = pageLayersRepository.findByPageIdAndLayerId(pageId, layerId).orElseThrow(
                () -> new EntityNotFoundException(
                        "Unable to find " + PageLayer.class.getName() + " with page id '" + pageId + " and layer id " +
                                layerId + "'!"));
        Page page = pageRepository.findById(pageId).orElseThrow(() -> new EntityNotFoundException(
                "Unable to find " + Page.class.getName() + " with id '" + pageId + "'!"));

        if (!eventParticipationService.isPageAccessible(pageId))
            transcriptionPermissionService.checkTranscribeAccess(page.getTranscription().getId());

        User user = loggedInUserHolder.getLoggedInUser();
        Verse verse = new Verse();
        verse.setBoundingBox(BoundingBoxUtils.convertBoundingBox(verseCreationDto.getBoundingBox()));
        verse.setFormatting(new Formatting());
        verse.setStatus(VerseStatus.NONE);
        verse.setPage(page);
        verse.setAuthor(user);
        verse.setPageLayer(pageLayer);
        verse.setType(verseCreationDto.getType());

        verse = doAddVerses(List.of(verse)).get(0);
        Verse verseForUndo = verse;
        Operation undoRedoOp = new Operation()
                .undo(() -> doDeleteVerses(List.of(verseForUndo.getId())))
                .redo(() -> doAddVerses(List.of(verseForUndo)));
        undoRedoService.addOperation(pageLayer.getId(), undoRedoOp);

        blockService.verseAdded(verse, undoRedoOp);

        return pageLayer.getId();
    }

    private List<Verse> doAddVerses(List<Verse> verses) {
        Page page = pageRepository.getById(verses.get(0).getPage().getId());
        Long layerId = verses.get(0).getPageLayer().getLayer().getId();

        List<Verse> savedVerses = new ArrayList<>();
        List<Long> savedIds = new ArrayList<>();
        List<Verse> otherVerses = verseRepository.findByPageIdAndLayerIdOrderByVerseNoAsc(page.getId(), layerId);
        for (Verse verse : verses) {
            for (Index index : verse.getIndexes()) {
                indexRepository.findById(index.getId())
                        .orElseGet(() -> indexRepository.save(index))
                        .addVerse(verse);
            }

            for (Word word : verse.getWords()) {
                for (Index index : word.getIndexes()) {
                    indexRepository.findById(index.getId())
                            .orElseGet(() -> indexRepository.save(index))
                            .addWord(word);
                }
            }

            VerseNumberFinder.computeAndSetVerseNo(verse, otherVerses);

            VerseContent currentVerseContent = verse.getCurrentVerseContent();
            VerseContent initialVerseContent = verse.getInitialVerseContent();
            verse.setCurrentVerseContent(null);
            verse.setInitialVerseContent(null);
            verse = verseRepository.save(verse);
            verse.setCurrentVerseContent(saveVerseContentHistory(currentVerseContent, verse));
            verse.setInitialVerseContent(saveVerseContentHistory(initialVerseContent, verse));
            savedVerses.add(verse);
            savedIds.add(verse.getId());
        }

        if (page.isEmpty()) {
            page.setEmpty(false);
        }

        modificationTrackingService.trackModification(verses.get(0).getPageLayer().getId());

        transcriptionExportService.resetTranscriptionExportStatus(page.getTranscription().getId());

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Created Verses with id - {} for Page with id - {} and Layer with id - {}.", savedIds,
                page.getId(), layerId);
        LogAction.clearMDCAction();

        return savedVerses;
    }

    private VerseContent saveVerseContentHistory(VerseContent verseContent, Verse verse) {
        if (verseContent != null) {
            verseContent.setOldContent(saveVerseContentHistory(verseContent.getOldContent(), verse));
            verseContent.setVerse(verse);
            return verseContentRepository.save(verseContent);
        }
        return null;
    }

    public void updateVerseCoordinates(Long verseId, BoundingBoxCore boundingBox) {
        Verse verse = getVerseToMakeAction(verseId);

        BoundingBoxCore oldBoundingBox = BoundingBoxUtils.convertBoundingBox(verse.getBoundingBox());
        Operation undoRedoOp = new Operation()
                .undo(() -> updateVerseCoordinates(verseId, oldBoundingBox))
                .redo(() -> updateVerseCoordinates(verseId, boundingBox));
        undoRedoService.addOperation(verse.getPageLayer().getId(), undoRedoOp);

        verse.setBoundingBox(BoundingBoxUtils.convertBoundingBox(boundingBox));

        transcriptionExportService.resetTranscriptionExportStatus(verse.getPage().getTranscription().getId());

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Updated coordinates of Verse with id - {}.", verse.getId());
        LogAction.clearMDCAction();

        verseRepository.save(verse);
    }


    public PageLayer.Id moveVersesUp(List<Long> versesIds, int times) throws IncorrectPositionException {
        List<Verse> verses = getVersesToMakeAction(versesIds);
        Verse firstVerse = verses.get(0);
        times = Math.min(times, firstVerse.getVerseNo() - 1);
        if (times <= 0) {
            throw new IncorrectPositionException("Verses already at the top", "cannotMoveFirstUp");
        }

        List<Verse> versesForUndo = verses;
        int timesForUndo = times;
        Operation undoRedoOp = new Operation()
                .undo(() -> doMoveVerses(versesForUndo, +timesForUndo))
                .redo(() -> doMoveVerses(versesForUndo, -timesForUndo));

        verses = doMoveVerses(verses, -times);

        blockService.versesMoved(verses, -times, undoRedoOp);

        undoRedoService.addOperation(firstVerse.getPageLayer().getId(), undoRedoOp);

        return firstVerse.getPageLayer().getId();
    }

    public PageLayer.Id moveVersesDown(List<Long> versesIds, int times) throws IncorrectPositionException {
        List<Verse> verses = getVersesToMakeAction(versesIds);
        Verse lastVerse = verses.get(verses.size() - 1);
        int count = verseRepository.countByPageLayer(lastVerse.getPageLayer());
        times = Math.min(times, count - lastVerse.getVerseNo());
        if (times <= 0) {
            throw new IncorrectPositionException("Verses already at the bottom", "cannotMoveLastDown");
        }

        List<Verse> versesForUndo = verses;
        int timesForUndo = times;
        Operation undoRedoOp = new Operation()
                .undo(() -> doMoveVerses(versesForUndo, -timesForUndo))
                .redo(() -> doMoveVerses(versesForUndo, +timesForUndo));

        verses = doMoveVerses(verses, +times);

        blockService.versesMoved(verses, times, undoRedoOp);

        undoRedoService.addOperation(lastVerse.getPageLayer().getId(), undoRedoOp);

        return lastVerse.getPageLayer().getId();
    }

    private List<Verse> getVersesToMakeAction(List<Long> versesIds) {
        if (versesIds == null || versesIds.isEmpty()) {
            throw new ValidationException("List of versesIds cannot be null or empty");
        }
        Verse verse0 = getVerseToMakeAction(versesIds.get(0));
        List<Verse> verses = verseRepository.findAllById(versesIds);

        if (verses.size() != versesIds.size()) {
            throw new ValidationException("Not all verses have been found");
        }

        for (Verse verse : verses) {
            if (!verse.getPageLayer().getId().equals(verse0.getPageLayer().getId())) {
                throw new ValidationException("Cannot move verses from different layers");
            }
        }

        verses.sort(Comparator.comparingInt(Verse::getVerseNo));
        for (int i = 1; i < verses.size(); i++) {
            if (verses.get(i).getVerseNo() != verses.get(i - 1).getVerseNo() + 1) {
                throw new ValidationException("Verses must be in consecutive and continuous order");
            }
        }

        return verses;
    }

    private List<Verse> doMoveVerses(List<Verse> verses, int distance) {
        PageLayer pageLayer = verses.get(0).getPageLayer();
        int otherVersesNoStart =
                distance < 0 ? verses.get(0).getVerseNo() + distance : verses.get(verses.size() - 1).getVerseNo() + 1;
        int otherVersesNoEnd =
                distance < 0 ? verses.get(0).getVerseNo() - 1 : verses.get(verses.size() - 1).getVerseNo() + distance;
        List<Verse> otherVerses = verseRepository.findByPageLayerIdAndVerseNoBetween(
                pageLayer.getId(), otherVersesNoStart, otherVersesNoEnd);

        for (Verse verse : verses) {
            verse.setVerseNo(verse.getVerseNo() + distance);
        }
        for (Verse otherVerse : otherVerses) {
            otherVerse.setVerseNo(otherVerse.getVerseNo() - Integer.signum(distance) * verses.size());
        }
        verses = verseRepository.saveAll(verses);
        verseRepository.saveAll(otherVerses);
    
        transcriptionExportService.resetTranscriptionExportStatus(pageLayer.getPage().getTranscription().getId());
    
        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Moved verses with id - {} in direction {}.",
                verses.stream().map(Verse::getId).collect(Collectors.toList()), distance);
        LogAction.clearMDCAction();
        return verses;
    }

    public void setVerseAsImage(Long verseId) {
        Verse verse = getVerseToMakeAction(verseId);
        verse.setCurrentVerseContent(null);
        //tODO: uncomment?
//        verse.setStatus(VerseStatus.VERIFIED);
        //TODO: set current user ?
//        verse.setAuthorOfLastStatusModification(user);
        clearVerseFormatting(verse);
        verse.setImage(true);
        verseRepository.save(verse);

        transcriptionExportService.resetTranscriptionExportStatus(verse.getPage().getTranscription().getId());

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Marked Verse with id - {} as image.", verse.getId());
        LogAction.clearMDCAction();
    }


    public void clearVerseFormatting(Verse verse) {
        verse.getFormatting().setFormeWork(null);
        verse.getFormatting().setHeading(null);
        verse.getFormatting().setTabularForm(null);
        verse.getFormatting().setEndOfParagraph(false);
        verse.getFormatting().setListForm(null);
        verse.getFormatting().setImageCaption(false);

        verse.getFormatting().setMarginalia(false);
        verse.getFormatting().setBold(false);
        verse.getFormatting().setItalic(false);
        verse.getFormatting().setUnderline(false);
        verse.getFormatting().setStrikethrough(false);
        verse.getFormatting().setAnnotation(false);

        verse.getFormatting().setNotatedMusic(false);

        verse.setImage(false);

        transcriptionExportService.resetTranscriptionExportStatus(verse.getPage().getTranscription().getId());
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public VersesAndBlocksDto changeVersesStatus(List<Long> verseIds, VerseStatus status) {
        Verse firstVerse = transactionTemplate.execute(
                ts -> verseRepository.findById(verseIds.get(0)).orElseThrow(EntityNotFoundException::new));
        PageLayer.Id pageLayerId = firstVerse.getPageLayer().getId();
        VersesAndBlocksDto result = doChangeVersesStatus(verseIds, pageLayerId, status);

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Changed status of {} Verses between ids - {}-{}. New status - {}", verseIds.size(),
                verseIds.get(0), verseIds.get(verseIds.size() - 1), status.name());
        LogAction.clearMDCAction();

        return result;
    }

    private VersesAndBlocksDto doChangeVersesStatus(List<Long> verseIds, PageLayer.Id pageLayerId, VerseStatus status) {
        Lock lock = pageStatusLock.get(pageLayerId.getPageId());
        lock.lock();
        try {
            transactionTemplate.executeWithoutResult(ts -> {
                List<Verse> verses = getVersesToMakeAction(verseIds);
                PageLayer pageLayer = verses.get(0).getPageLayer();
                if (!verses.stream().allMatch(v -> v.getPageLayer().getId().equals(pageLayerId))) {
                    throw new ValidationException("Cannot change status of verses from different layers");
                }
                ViewMode viewMode =
                        transcriptionPermissionService.findViewMode(pageLayer.getPage().getTranscription().getId());
                User user = loggedInUserHolder.getLoggedInUser();
                Page page =
                        pageRepository.findById(pageLayer.getPage().getId()).orElseThrow(EntityNotFoundException::new);
                setDefaultLang(verses);
                setStatus(verses, viewMode, status, user);

                Event event = eventParticipationService.getCurrentEvent();
                if (event != null && status == VerseStatus.TRANSCRIBED
                        && eventParticipationService.isPageAccessible(page.getId())) {
                    verses.forEach(v -> v.setModificationLastEvent(event));
                    verses.forEach(v -> v.setModificationLastEventAttendee(user));
                }

                modificationTrackingService.trackModification(pageLayerId);
                verseRepository.saveAll(verses);

                transcriptionExportService.resetTranscriptionExportStatus(page.getTranscription().getId());
            });
            transactionTemplate.executeWithoutResult(ts -> {
                progressService.updatePageProgress(pageLayerId.getPageId());
            });
        } finally {
            lock.unlock();
        }
        return BeanLoader.getBean(VerseService.class).getVersesAndBlocksDto(pageLayerId);
    }

    private void setDefaultLang(List<Verse> verses) {
        verses = verses.stream().filter(verse -> verse.getLang() != null).collect(Collectors.toList());
        if (!verses.isEmpty()) {
            Transcription transcription = verses.get(0).getPage().getTranscription();

            String defaultLang;
            if (transcription.getAnalyticInfo().getLanguage() != null) {
                defaultLang = transcription.getAnalyticInfo().getLanguage().getCode();
            } else if (transcription.getMonographicInfo().getLanguage() != null) {
                defaultLang = transcription.getMonographicInfo().getLanguage().getCode();
            } else if (transcription.getLastSetLanguageCode() != null) {
                defaultLang = transcription.getLastSetLanguageCode();
            } else {
                defaultLang = propertyService.getDefaultTranscriptionLanguage();
            }
            verses.forEach(verse -> verse.setLang(defaultLang));
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public VersesAndBlocksDto changeAllVerseOnPageLayerStatus(long pageId, long layerId, VerseStatus newVerseStatus) {
        PageLayer.Id pageLayerId = new PageLayer.Id(pageId, layerId);
        List<Long> verseIds = transactionTemplate.execute(
                ts -> verseRepository.findVerseIdsByPageLayerId(pageLayerId));
        if (verseIds.isEmpty()) {
            throw new EntityNotFoundException("No verses found on this layer!");
        }
        VersesAndBlocksDto result = doChangeVersesStatus(verseIds, pageLayerId, newVerseStatus);

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Updated status of all Verses from Page with id - {} and Layer with id - {}. New status - {}.",
                pageId, layerId, newVerseStatus.name());
        LogAction.clearMDCAction();

        return result;
    }

    /**
     * set next verse status . Status loop: [Optional:NONE]-TRANSCRIBED-VERIFIED-INCORRECT
     */
    private static VerseStatus checkNextStatus(Verse verse, ViewMode viewMode, VerseStatus targetStatus) {
        VerseStatus verseStatus = verse.getStatus();
        if (viewMode == ViewMode.VERIFY || viewMode == ViewMode.ADMIN) {
            //logic for moderator / admin
            if (targetStatus != null) {
                return targetStatus;
            }
            switch (verseStatus) {
                case NONE:
                case TRANSCRIBED:
                    return VerseStatus.VERIFIED;
                case VERIFIED:
                    return VerseStatus.INCORRECT;
                case INCORRECT:
                    return VerseStatus.TRANSCRIBED;
            }
        } else {
            //logic for transcriber
            if (targetStatus != null) {
                if (!EnumSet.of(VerseStatus.NONE, VerseStatus.TRANSCRIBED).contains(targetStatus))
                    throw new ValidationException("Change to " + targetStatus + " not allowed");
                return targetStatus;
            }
            switch (verseStatus) {
                case NONE:
                case INCORRECT:
                case VERIFIED:
                    return VerseStatus.TRANSCRIBED;
                case TRANSCRIBED:
                    return VerseStatus.NONE;
            }
        }
        throw new RuntimeException("problem with " + verseStatus + " -> " + targetStatus);
    }

    private List<Verse> doDeleteVerses(List<Long> verseIds) {
        Verse verse0 = getVerseToMakeAction(verseIds.get(0));

        List<Verse> verses = verseRepository.findAllById(verseIds);
        if (verses.stream().map(v -> v.getPageLayer().getId()).distinct().count() > 1)
            throw new ValidationException("All deleted verses must be in the same layer!");

        Page page = verse0.getPage();
        Long layerId = verse0.getPageLayer().getLayer().getId();

        for (Verse verse : verses) {
            Set<Index> indexes = new HashSet<>(verse.getIndexes());
            verse.getIndexes().clear();
            for (Index index : indexes) {
                index.getVerses().remove(verse);
                if (index.getVerses().isEmpty())
                    indexRepository.delete(index);
            }

            List<Word> words = new ArrayList<>(verse.getWords());
            for (Word word : words) {
                Set<Index> wordIndexes = new HashSet<>(word.getIndexes());
                word.getIndexes().clear();
                for (Index index : wordIndexes) {
                    index.removeWord(word);
                }
                wordRepository.delete(word);
                word.setIndexes(wordIndexes);
            }
            verse.getWords().clear();

            List<VerseContent> verseContents = verseContentRepository.findByVerseId(verse.getId());
            VerseContent currentVerseContent = verse.getCurrentVerseContent();
            VerseContent initialVerseContent = verse.getInitialVerseContent();
            verse.setCurrentVerseContent(null);
            verse.setInitialVerseContent(null);
            verseContentRepository.deleteAll(verseContents);

            verseRepository.delete(verse);

            verse.setIndexes(indexes); // for undo/redo restore
            verse.setWords(words);
            verse.setCurrentVerseContent(currentVerseContent);
            verse.setInitialVerseContent(initialVerseContent);
        }

        List<Verse> otherVerses = verseRepository.findByPageIdAndLayerIdOrderByVerseNoAsc(page.getId(), layerId);
        for (int i = 0; i < otherVerses.size(); i++)
            otherVerses.get(i).setVerseNo(i + 1);

        transcriptionExportService.resetTranscriptionExportStatus(page.getTranscription().getId());

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Removed Verses with id - {}.", verseIds);
        LogAction.clearMDCAction();
        return verses;
    }

    public PageLayer.Id deleteVerses(List<Long> versesIds) {
        List<Comment> comments = commentRepository.findByVerseIdIn(versesIds);
        List<Verse> verses = doDeleteVerses(versesIds);
        verses.sort(Comparator.comparing(Verse::getVerseNo));
        Operation undoRedoOp = new Operation()
                .undo(() -> commentRepository.saveAll(comments))
                .undo(() -> doAddVerses(verses))
                .redo(() -> doDeleteVerses(versesIds));
        for (int i = verses.size() - 1; i >= 0; i--) {
            blockService.verseRemoved(verses.get(i), undoRedoOp);
        }
        PageLayer pageLayer = verses.get(0).getPageLayer();
        undoRedoService.addOperation(pageLayer.getId(), undoRedoOp);

        return pageLayer.getId();
    }

    @Transactional(readOnly = true)
    public VersesAndBlocksDto getVersesAndBlocksDto(PageLayer.Id pageLayerId) {
        var result = new VersesAndBlocksDto();
        PageLayer pageLayer = pageLayersRepository.findById(pageLayerId).orElseThrow();
        result.setVerses(completeVerses(pageLayer.getPage()));
        result.setBlocks(blockService.getBlocks(pageLayer, result.getVerses()));
        return result;
    }

    @UtilityClass
    private class VerseNumberFinder {
        public void computeAndSetVerseNo(Verse verse, List<Verse> verses) {

            if (verses.isEmpty()) {
                verse.setVerseNo(1);
                verses.add(verse);
                return;
            }

            int insertIndex;
            if (verse.getVerseNo() != null) {
                insertIndex = (int) verses.stream().filter(v -> v.getVerseNo() < verse.getVerseNo()).count();
            } else {
                insertIndex = findInsertPositionIndex(verse, verses);
                if (insertIndex < verses.size()) {
                    verse.setVerseNo(verses.get(insertIndex).getVerseNo());
                } else {
                    verse.setVerseNo(verses.get(verses.size() - 1).getVerseNo() + 1);
                }
            }

            List<Verse> versesAfter = verses.subList(insertIndex, verses.size());
            versesAfter.forEach(v -> v.setVerseNo(v.getVerseNo() + 1));
            verses.add(insertIndex, verse);
        }

        int findInsertPositionIndex(Verse verse, List<Verse> verses) {
            int neightborIndex = findNeighbor(verse, verses, true);
            if (neightborIndex >= 0)
                return neightborIndex + 1;

            neightborIndex = findNeighbor(verse, verses, false);
            if (neightborIndex >= 0)
                return neightborIndex;

            BoundingBox b = verse.getBoundingBox();
            int cX = (b.getUpperLeftX() + b.getLowerRightX()) / 2;
            int cY = (b.getUpperLeftY() + b.getLowerRightY()) / 2;

            long minDist = Long.MAX_VALUE;
            int bestI = 0;
            for (int i = 0; i < verses.size(); i++) {
                BoundingBox nb = verses.get(i).getBoundingBox();
                int ncX = (nb.getUpperLeftX() + nb.getLowerRightX()) / 2;
                int ncY = (nb.getUpperLeftY() + nb.getLowerRightY()) / 2;

                long dist = 1L * (ncX - cX) * (ncX - cX) + 1L * (ncY - cY) * (ncY - cY);
                if (dist < minDist) {
                    bestI = i;
                    minDist = dist;
                }
            }

            BoundingBox nb = verses.get(bestI).getBoundingBox();
            int nCenterX = (nb.getUpperLeftX() + nb.getLowerRightX()) / 2;
            boolean goUp = nCenterX > cX;
            while (true) {
                int neighborIndex = findNeighbor(verses.get(bestI), verses, goUp);
                if (neighborIndex < 0)
                    break;
                bestI = neighborIndex;
            }
            return goUp ? bestI : bestI + 1;
        }

        int findNeighbor(Verse verse, List<Verse> verses, boolean up) {
            BoundingBox b = verse.getBoundingBox();
            int bLeft = b.getUpperLeftX();
            int bRight = b.getLowerRightX();
            int bTop = b.getUpperLeftY();
            int bestIndex = -1;
            int bestDist = Integer.MAX_VALUE;
            for (int i = 0; i < verses.size(); i++) {
                if (verses.get(i) == verse)
                    continue;
                BoundingBox nb = verses.get(i).getBoundingBox();
                int nLeft = nb.getUpperLeftX();
                int nRight = nb.getLowerRightX();
                int nTop = nb.getUpperLeftY();
                int common = BoundingBoxUtils.getCommonSegment(nLeft, nRight, bLeft, bRight);
                int dist = up ? bTop - nTop : nTop - bTop;
                if ((common > (bRight - bLeft) * 50 / 100 || common > (nRight - nLeft) * 50 / 100)
                        && dist > 0 && dist < bestDist) {
                    bestDist = dist;
                    bestIndex = i;
                }
            }
            return bestIndex;
        }
    }
}
