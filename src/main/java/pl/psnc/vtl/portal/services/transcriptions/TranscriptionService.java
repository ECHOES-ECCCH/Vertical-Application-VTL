package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import pl.psnc.vtl.portal.conf.logging.LogAction;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.dto.indexes.IndexCore;
import pl.psnc.vtl.portal.dto.indexes.IndexDto;
import pl.psnc.vtl.portal.dto.layers.LayerCore;
import pl.psnc.vtl.portal.dto.layers.LayerDto;
import pl.psnc.vtl.portal.dto.permissions.ViewMode;
import pl.psnc.vtl.portal.dto.transcriptions.*;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.permissions.TranscriptionGroup;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUser;
import pl.psnc.vtl.portal.model.transcriptions.CreationMode;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.transcriptions.TranscriptionExportStatus;
import pl.psnc.vtl.portal.model.transcriptions.TranscriptionInfo;
import pl.psnc.vtl.portal.model.users.UserRole;
import pl.psnc.vtl.portal.model.verses.OcrMode;
import pl.psnc.vtl.portal.repositories.iiiftools.DeleteTranscriptionImagesEmailService;
import pl.psnc.vtl.portal.repositories.iiiftools.IIIFToolsException;
import pl.psnc.vtl.portal.repositories.iiiftools.IIIFToolsRepository;
import pl.psnc.vtl.portal.repositories.iiiftools.RemoveFileResponse;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.*;
import pl.psnc.vtl.portal.repositories.users.UserGroupRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.services.indexes.IndexService;
import pl.psnc.vtl.portal.services.layers.LayerMapperImpl;
import pl.psnc.vtl.portal.services.layers.LayerService;
import pl.psnc.vtl.portal.services.pages.*;
import pl.psnc.vtl.portal.services.parsers.ocr.OcrLevel;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;
import pl.psnc.vtl.portal.services.progress.ProgressService;
import pl.psnc.vtl.portal.services.properties.PropertyService;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;
import pl.psnc.vtl.portal.services.users.UserService;
import pl.psnc.vtl.portal.services.verses.UndoRedoService;

import javax.persistence.EntityNotFoundException;
import javax.validation.ValidationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static pl.psnc.vtl.portal.services.registration.LoggedInUserHolder.withLoggedInUser;
import static pl.psnc.vtl.portal.utils.ThreadUtils.withMdc;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TranscriptionService {

    private final TranscriptionRepository transcriptionRepository;

    private final TranscriptionInfoRepository transcriptionInfoRepository;

    private final TranscriptionPermissionService transcriptionPermissionService;

    private final TranscriptionValidator transcriptionValidator;

    private final TranscriptionMetadataValidator transcriptionMetadataValidator;

    private final TranscriptionInfoValidator transcriptionInfoValidator;

    private final PageService pageService;

    private final PageImportService pageImportService;

    private final ImportPageRecognizerFactory importPageRecognizerFactory;

    private final PageListService pageListService;

    private final PageRepository pageRepository;

    private final VerseRepository verseRepository;

    private final TranscriptionUserRepository transcriptionUserRepository;

    private final TranscriptionGroupRepository transcriptionGroupRepository;

    private final UserGroupRepository userGroupRepository;

    private final IIIFToolsRepository iiifToolsRepository;

    private final PropertyService propertyService;

    private final DeleteTranscriptionImagesEmailService deleteTranscriptionImagesEmailService;

    private final TranscriptionUserLastPageRepository transcriptionUserLastPageRepository;
    private final UserLastTranscriptionRepository userLastTranscriptionRepository;

    private final TranscriptionContinuationService transcriptionContinuationService;

    private final TranscriptionProcessingCoordinator transcriptionProcessingCoordinator;

    private final ModificationTrackingService modificationTrackingService;

    private final ProgressService progressService;

    private final UndoRedoService undoRedoService;

    private final UserService userService;

    private final IndexService indexService;

    private final LayerService layerService;

    private final LoggedInUserHolder loggedInUserHolder;
    private final TransactionTemplate transactionTemplate;

    @Value("${vtl.pagination.pages.default-perpage}")
    private Integer pagesDefaultPerpage;

    @Value("${quota.transcription}")
    private Integer translationQuota;

    private static final String TRANSCRIPTION_FIRST_LAYER_DEFAULT_NAME = "Nieokreślona";
    private static final String TRANSCRIPTION_FIRST_LAYER_DEFAULT_DESCRIPTION = "Brak opisu";

    /**
     * Require transcription to exists
     *
     * @param transcriptionId transcription primary id
     * @return Transcription object
     * @throws EntityNotFoundException if Transcription with given id doesn't exists
     */
    public Transcription requireTranscription(Long transcriptionId) {
        return transcriptionRepository.findById(transcriptionId).orElseThrow(() -> new EntityNotFoundException(
                "Unable to find " + Transcription.class.getName() + " with id '" + transcriptionId + "'!"));
    }

    @Transactional(readOnly = true)
    public TranscriptionRegularDto getTranscription(Long transcriptionId, int page, int perpage) {
        Transcription transcription = this.requireTranscription(transcriptionId);
        completeTranscription(transcription, true, page, perpage);
        return new TranscriptionRegularDto(transcription);
    }

    @Transactional(readOnly = true)
    public void completeTranscription(Transcription transcription, boolean fillPages, int page, int perpage) {
        progressService.computeProgress(transcription);
        if (fillPages) {
            transcription.setPages(pageListService.getPages(transcription.getId(), page, perpage));
        }
        ViewMode viewMode = transcriptionPermissionService.checkReadAccess(transcription.getId());
        transcription.setViewMode(viewMode);
        transcription.setContinuation(transcriptionContinuationService.constructContinuation(transcription));

        HashSet<Long> userIds = new HashSet<>();
        transcriptionUserRepository.findByTranscriptionId(transcription.getId())
                .forEach(tu -> userIds.add(tu.getUser().getId()));
        List<Long> groupIds = transcriptionGroupRepository.findByTranscriptionId(transcription.getId())
                .stream().map(tg -> tg.getGroup().getId()).collect(Collectors.toList());
        userGroupRepository.findByGroupIdIn(groupIds).forEach(ug -> userIds.add(ug.getUser().getId()));
        transcription.setUserCount(userIds.size());
        if (transcription.getLayers() == null)
            transcription.setLayers(layerService.getAllLayersPerTranscription(transcription.getId()));
        transcription.setAsyncProgressInfo(transcriptionProcessingCoordinator.getProcessing(transcription.getId()));
    }

    public Transcription createTranscription(TranscriptionCore newTranscription, CreationMode creationMode) {
        Transcription transcription = transcriptionValidator.validate(newTranscription, null, true);

        transcription.setProjectId(newTranscription.getProjectId());
        transcription.setExternalId(newTranscription.getExternalId());
        transcription.setExternalUrl(newTranscription.getExternalUrl());
        transcription.setExternalMetadataUrl(newTranscription.getExternalMetadataUrl());
        transcription.setExternalMetadataType(newTranscription.getExternalMetadataType());
        transcription.setTeiHeader(newTranscription.getTeiHeader());
        transcription.setTeiBody(newTranscription.getTeiBody());
        transcription.setTitleLabel(TranscriptionHelper.findTitleLabel(transcription));
        transcription.setAuthorsLabel(TranscriptionHelper.findAuthorsLabel(transcription));

        transcription.setCreationMode(creationMode);
        transcription.setCreator(loggedInUserHolder.getLoggedInUser());
        transcription.setExportStatus(newTranscription.isExternalEdit() ? TranscriptionExportStatus.NEW : TranscriptionExportStatus.UNDEFINED);

        LocalDateTime now = LocalDateTime.now();
        transcription.setDateCreated(now);
        transcription.setLastModificationDate(now);

        transcription.setDatePublished(null);
        transcription.setHidden(true);

        transcription.setQuota(translationQuota * 1000000L);
        transcription = transcriptionRepository.save(transcription);

        TranscriptionUser transcriptionUser = new TranscriptionUser();
        transcriptionUser.setTranscription(transcription);
        transcriptionUser.setUser(transcription.getCreator());
        transcriptionUser.setRole(UserRole.builder().code("admin").build());
        transcriptionUserRepository.save(transcriptionUser);

        layerService.createLayer(new LayerCore(transcription.getId(), TRANSCRIPTION_FIRST_LAYER_DEFAULT_NAME, TRANSCRIPTION_FIRST_LAYER_DEFAULT_DESCRIPTION, null));

        modificationTrackingService.trackModification(transcription);

        LogAction.setMDCAction(LogAction.CREATE_TRANSCRIPTION);
        log.info("Created Transcription with id - {}.", transcription.getId());
        LogAction.clearMDCAction();

        return transcription;
    }

    public Transcription updateTranscription(Long transcriptionId, TranscriptionCore newTranscription) {
        Transcription t = this.requireTranscription(transcriptionId);
        transcriptionPermissionService.checkAdminAccess(transcriptionId);
        checkTranscriptionIsUpdatable(t);

        Transcription validatedTranscription = transcriptionValidator.validate(newTranscription, transcriptionId,
                false);
        validatedTranscription.setTitleLabel(TranscriptionHelper.findTitleLabel(validatedTranscription));
        validatedTranscription.setAuthorsLabel(TranscriptionHelper.findAuthorsLabel(validatedTranscription));
        transcriptionRepository.save(validatedTranscription);

        modificationTrackingService.trackModification(t);

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Modified Transcription with id - {} from TranscriptionCore.", validatedTranscription.getId());
        LogAction.clearMDCAction();

        return validatedTranscription;
    }

    public TranscriptionInfo updateTranscriptionInfo(Long transcriptionId,
                                                     TranscriptionInfoCore transcriptionInfoCore) {
        Transcription transcription = requireTranscription(transcriptionId);
        transcriptionPermissionService.checkAdminAccess(transcriptionId);
        checkTranscriptionIsUpdatable(transcription);

        TranscriptionInfo transcriptionInfo = transcriptionInfoValidator.validate(transcriptionInfoCore, transcription);
        transcriptionInfoRepository.save(transcriptionInfo);
        modificationTrackingService.trackModification(transcription);

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Modified Transcription with id - {} from TranscriptionInfoCore.", transcription.getId());
        LogAction.clearMDCAction();

        return transcriptionInfo;
    }


    public TranscriptionMetadataCore updateTranscriptionMetadata(Long transcriptionId,
            TranscriptionMetadataCore transcriptionMetadataCore, boolean checkUpdatable) {
        Transcription transcription = this.requireTranscription(transcriptionId);
        transcriptionPermissionService.checkAdminAccess(transcriptionId);
        if (checkUpdatable)
            checkTranscriptionIsUpdatable(transcription);

        transcription = transcriptionMetadataValidator.validate(transcriptionMetadataCore, transcription);
        transcription.setTitleLabel(TranscriptionHelper.findTitleLabel(transcription));
        transcription.setAuthorsLabel(TranscriptionHelper.findAuthorsLabel(transcription));
        transcriptionRepository.save(transcription);
        modificationTrackingService.trackModification(transcription);

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Modified Transcription with id - {} from TranscriptionMetadataCore.", transcription.getId());
        LogAction.clearMDCAction();

        return transcriptionMetadataCore;
    }

    @Transactional(propagation = Propagation.NEVER)
    public void addPagesToTranscription(Long transcriptionId, MultipartFile multipartFile) throws IOException,
            WrongFileTypeException {

        if (multipartFile.isEmpty()) {
            throw new WrongFileTypeException("No provided file for transcription " + transcriptionId + "!",
                    "missingFile", null);
        }
        final String OPERATION = "LocalImport";
        transcriptionProcessingCoordinator.startProcessing(transcriptionId, OPERATION);

        Transcription[] transcription = {null};
        List<String> filenames = new ArrayList<>();
        try {
            transactionTemplate.executeWithoutResult(status -> {
                transcription[0] = this.requireTranscription(transcriptionId);

                userService.validateUsedSpaceQuota(transcription[0].getCreator());
                transcriptionValidator.validateUsedTranscriptionSpaceQuota(transcriptionId);
                transcriptionPermissionService.checkAdminAccess(transcriptionId);

                transcription[0].setViewMode(transcriptionPermissionService.findViewMode(transcriptionId));
                progressService.computeProgress(transcription[0]);
                transcription[0].setPages(pageListService.getPages(transcriptionId, 1, pagesDefaultPerpage));
            });

            if (multipartFile.getOriginalFilename().toLowerCase().endsWith(".zip")) {
                try (ZipInputStream zis = new ZipInputStream(multipartFile.getInputStream())) {
                    ZipEntry ze = zis.getNextEntry();
                    while (ze != null) {

                        //This is workaround for indexed djvu files from dlibra
                        if (ze.getName().equals("_index.djvu") || ze.getName().equals("index.djvu")) {
                            log.info("Skipping _index.djvu file import for Transcription with id - {}", transcriptionId);
                        } else {
                            String savedFileFileName = saveFile(transcriptionId, ze.getName(), zis);
                            if (savedFileFileName != null) {
                                filenames.add(savedFileFileName);
                            } else {
                                log.info("Skipping {} file import for Transcription with id - {}", ze.getName(), transcriptionId);
                            }
                        }

                        zis.closeEntry();
                        ze = zis.getNextEntry();
                    }
                }
            } else {
                try (InputStream inStream = multipartFile.getInputStream()) {
                    String savedFileFileName = saveFile(transcriptionId, multipartFile.getOriginalFilename(), inStream);
                    if (savedFileFileName != null) {
                        filenames.add(savedFileFileName);
                    } else {
                        log.info("Skipping {} file import for Transcription with id - {}", multipartFile.getOriginalFilename(), transcriptionId);
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            // no need to remember processing as failed, user will see message immediately
            transcriptionProcessingCoordinator.processingFinished(transcriptionId, OPERATION, null);
            throw e;
        }

        CompletableFuture.runAsync(withMdc(withLoggedInUser(() -> {
            try {
                Map<String, List<String>> fileToPyramidNames =
                        iiifToolsRepository.preparePyramids(transcriptionId, filenames, count ->
                                transcriptionProcessingCoordinator.progress(transcriptionId, OPERATION,
                                100 * count / filenames.size()));

                transactionTemplate.executeWithoutResult(status -> {
                    pageService.createPages(transcription[0], fileToPyramidNames);

                    userService.updateUsedSpaceQuota(transcription[0].getCreator(), multipartFile.getSize());
                    updateUsedTranscriptionSpaceQuota(multipartFile.getSize(), transcriptionId);

                    modificationTrackingService.trackModification(transcription[0]);
                });
                transcriptionProcessingCoordinator.processingFinished(transcriptionId, OPERATION, null);

                LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
                log.info("Imported pages from file for Transcription with id - {}.", transcriptionId);
                LogAction.clearMDCAction();
            } catch (Exception e) {
                log.error("Import failed for transcription {}", transcriptionId, e);
                transcriptionProcessingCoordinator.processingFinished(transcriptionId, OPERATION, e);
            }
        })));
    }

    private String saveFile(long transcriptionId, String filename, InputStream inStream) {
        filename = PageHelper.validateFileType(filename);

        if (filename == null) {
            return null;
        }

        if (pageRepository.existsByTranscriptionIdAndPathStartingWithIgnoreCase(transcriptionId,
                FilenameUtils.getBaseName(filename) + ".")) {
            throw new WrongFileTypeException(
                    "Provided file '" + filename + "' for transcription " + transcriptionId + " already exists!",
                    "fileAlreadyExists", FilenameUtils.getBaseName(filename) + ".*");
        }
        iiifToolsRepository.savePageImage(transcriptionId, inStream, filename);
        return filename;
    }

    public void deleteTranscription(Long transcriptionId) {
        Transcription transcription = transcriptionRepository.findById(transcriptionId).orElseThrow(
                () -> new EntityNotFoundException("Transcription with id " + transcriptionId + " does not exist"));

        if (!loggedInUserHolder.isCurrentlyLoggedUserSuperadmin()) {
            transcriptionPermissionService.checkAdminAccess(transcriptionId);
        }

        transcriptionUserLastPageRepository.deleteByTranscriptionId(transcriptionId);
        userLastTranscriptionRepository.deleteByTranscription(transcription);
        transcriptionUserRepository.deleteByTranscriptionId(transcriptionId);
        transcriptionGroupRepository.deleteByTranscriptionId(transcriptionId);

        layerService.deleteLayers(transcriptionId);

        transcriptionRepository.deleteById(transcriptionId);

        if(pageRepository.countByTranscriptionId(transcriptionId) > 0) {
            RemoveFileResponse response = deleteTranscriptionFiles(transcriptionId);
            if (response != null) {
                userService.updateUsedSpaceQuota(transcription.getCreator(), -response.getQuota());
            }
        }

        LogAction.setMDCAction(LogAction.REMOVE_TRANSCRIPTION);
        log.info("Removed Transcription with id - {}.", transcription.getId());
        LogAction.clearMDCAction();
    }


    private RemoveFileResponse deleteTranscriptionFiles(Long transcriptionId) {
        try {
            return iiifToolsRepository.deleteTranscriptionImages(transcriptionId);
        } catch (IIIFToolsException e) {
            deleteTranscriptionImagesEmailService.sendEmail(transcriptionId, null,
                    userService.getAllSuperadminUsersMails());
            return null;
        }
    }


    public TranscriptionPermissions getTranscriptionPermissions(long transcriptionId) {
        ViewMode viewMode = transcriptionPermissionService.checkTranscribeAccess(transcriptionId);
        Transcription transcription = this.requireTranscription(transcriptionId);

        List<TranscriptionGroup> transcriptionGroups = transcriptionGroupRepository.findByTranscriptionId(
                transcriptionId);
        List<TranscriptionUser> transcriptionUsers = transcriptionUserRepository.findByTranscriptionId(transcriptionId);
        transcription.setContinuation(transcriptionContinuationService.constructContinuation(transcription));
        return TranscriptionPermissions.builder().publiclyAvailable(!transcription.isHidden())
                .transcription(transcription).allowedGroups(transcriptionGroups).allowedUsers(transcriptionUsers)
                .viewMode(viewMode).transcriptionContinuation(transcription.getContinuation()).build();
    }


    public Transcription changeTranscriptionVisibility(long transcriptionId, boolean hidden) {
        Transcription transcription = this.requireTranscription(transcriptionId);

        transcriptionPermissionService.checkAdminAccess(transcriptionId);
        transcription.setHidden(hidden);
        transcription.setDatePublished(hidden ? null : LocalDateTime.now());
        transcriptionRepository.save(transcription);

        modificationTrackingService.trackModification(transcription);

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Changed visibility of Transcription with id - {}. New visibility - {}", transcriptionId, hidden ? "private" : "public");
        LogAction.clearMDCAction();

        return transcription;
    }


    public String getTranscriptionDefaultLanguage(long transcriptionId) {
        Transcription transcription = this.requireTranscription(transcriptionId);

        return ObjectUtils.firstNonNull(transcription.getLastSetLanguageCode(),
                transcription.getAnalyticInfo().getLanguage() != null ? transcription.getAnalyticInfo().getLanguage()
                        .getCode() : null,
                transcription.getMonographicInfo().getLanguage() != null ? transcription.getMonographicInfo()
                        .getLanguage().getCode() : null, propertyService.getDefaultTranscriptionLanguage());
    }


    /**
     * Execute OCR on selected pages of the transcription according to the given ocr
     * mode. Relative numbers of pages are supplied in a comma separated string
     * which allows also ranges like 3-6. When ocr mode is set to EMPTY_ONLY then
     * only pages without transcription are recognized.
     * @param transcriptionId transcription identifier
     * @param ocrMode         ocr mode
     * @param lang            language used for ocr
     * @param pages           relative numbers of pages
     * @param layerId         id of transcription layer
     * @param handleColumns
     */
    public void recognizeTranscriptionPages(long transcriptionId, OcrMode ocrMode, String lang, String pages,
            Long layerId, OcrLevel ocrLevel, boolean handleColumns) {

        transcriptionPermissionService.checkTranscribeAccess(transcriptionId);
        List<Integer> pageNums =
                PageHelper.parsePages(pages, pageRepository.countByTranscriptionId(transcriptionId) + 1);
        List<Long> pageIds = pageRepository.findByTranscriptionIdAndPageNoIn(transcriptionId, pageNums).stream()
                .map(Page::getId).collect(Collectors.toList());
        if (ocrMode == OcrMode.EMPTYONLY)
            pageIds.removeIf(p -> verseRepository.existsByPageIdAndPageLayerLayerId(p, layerId));
        if (pageIds.isEmpty()) {
            log.info("No pages to OCR for transcription {}.", transcriptionId);
            return;
        }
        for (Long pageId : pageIds)
            undoRedoService.reset(new PageLayer.Id(pageId, layerId));
        final String OPERATION = "OCR";
        transcriptionProcessingCoordinator.startProcessing(transcriptionId, OPERATION);
        CompletableFuture.runAsync(withMdc(withLoggedInUser(() -> {
            try {
                for (int i = 0; i < pageIds.size(); i++) {
                    Page page = pageService.getPage(transcriptionId, pageIds.get(i), ViewMode.TRANSCRIBE);
                    if (!page.isEmpty())
                        pageService.recognizePage(page, lang, ocrMode, layerId, ocrLevel, handleColumns);
                    transcriptionProcessingCoordinator.progress(transcriptionId, OPERATION, 100 * i / pageIds.size());
                }
                transcriptionProcessingCoordinator.processingFinished(transcriptionId, OPERATION, null);

                LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
                log.info("Completed OCR process for Transcription with id - {}.", transcriptionId);
                LogAction.clearMDCAction();
            } catch (Exception e) {
                log.error("OCR for transcription {} layer {} failed", transcriptionId, layerId, e);
                transcriptionProcessingCoordinator.processingFinished(transcriptionId, OPERATION, e);
            }
        })));
    }

    public IndexDto createIndex(long transcriptionId, IndexCore indexCore) {
        this.requireTranscription(transcriptionId);
        return indexService.createIndex(transcriptionId, indexCore);
    }


    public List<IndexDto> getAllIndexesForTranscription(long transcriptionId) {
        this.requireTranscription(transcriptionId);
        return indexService.getAllIndexesForTranscription(transcriptionId);
    }


    public List<IndexDto> searchIndexes(long transcriptionId, String phrase, String type) {
        this.requireTranscription(transcriptionId);
        return indexService.searchIndexes(transcriptionId, phrase, type);
    }

    public LayerDto updateLayer(long transcriptionId, long layerId, LayerCore layerCore) {
        this.requireTranscription(transcriptionId);
        transcriptionPermissionService.checkAdminAccess(transcriptionId);

        layerCore.setTranscriptionId(transcriptionId);

        return layerService.updateLayer(layerId, layerCore);
    }


    public void deleteLayer(long transcriptionId, long layerId) {
        this.requireTranscription(transcriptionId);
        transcriptionPermissionService.checkAdminAccess(transcriptionId);

        layerService.deleteLayer(layerId);
    }


    public List<LayerDto> getAllLayersPerTranscription(long transcriptionId) {
        transcriptionPermissionService.checkReadAccess(transcriptionId);
        return LayerMapperImpl.INSTANCE.toDto(layerService.getAllLayersPerTranscription(transcriptionId));
    }

    public LayerDto getLayer(long transcriptionId, long layerId) {
        transcriptionPermissionService.checkReadAccess(transcriptionId);
        return LayerMapperImpl.INSTANCE.toDto(layerService.getLayer(layerId));
    }

    /**
     * get summarize of user actions in the transcription
     *
     * @param transcriptionId
     * @return summarize
     */
    public List<TranscriptionSummarize> summarizeTranscription(long transcriptionId) {
        transcriptionPermissionService.checkReadAccess(transcriptionId);
        //TODO: hibernate proper pojo object mapping from native
        return transcriptionRepository.transcriptionSummarize(transcriptionId).stream().map(p -> {
            int i = 0;
            return new TranscriptionSummarize(p[i++].toString(), ((Boolean) p[i++]), p[i++].toString(),
                    ((BigInteger) (p[i++])).longValue(), ((BigInteger) (p[i++])).longValue(),
                    ((BigInteger) (p[i++])).longValue(), ((BigInteger) (p[i++])).longValue(),
                    ((BigInteger) (p[i++])).longValue(), ((BigInteger) (p[i++])).longValue(),
                    ((BigInteger) (p[i++])).longValue(), ((BigInteger) (p[i])).longValue());
        }).collect(Collectors.toList());
    }

    public void updateTranscriptionSpaceQuota(int quota, Long transcriptionId) {
        transcriptionPermissionService.checkAdminAccess(transcriptionId);
        Transcription transcription = this.requireTranscription(transcriptionId);
        transcription.setQuota(quota * 1000000L);
        transcriptionRepository.save(transcription);

        LogAction.setMDCAction(LogAction.MODIFY_DATA);
        log.info("Updated quota for Transcription with id - {}. New quota - {}", transcription.getId(), quota);
        LogAction.clearMDCAction();
    }

    public void updateUsedTranscriptionSpaceQuota(long quota, Long transcriptionId) {
        Transcription transcription = this.requireTranscription(transcriptionId);
        transcription.setUsedSpaceQuota(transcription.getUsedSpaceQuota() + quota);
        transcriptionRepository.save(transcription);
    }


    public void importIntoLayer(long transcriptionId, long layerId, TranscriptionPageContentType format,
            MultipartFile multipartFile, OcrLevel ocrLevel) throws Exception {
        this.requireTranscription(transcriptionId);
        transcriptionPermissionService.checkTranscribeAccess(transcriptionId);

        if (multipartFile.isEmpty()) {
            throw new WrongFileTypeException("No provided file for transcription " + transcriptionId + "!",
                    "missingFile", null);
        }

        String filename = multipartFile.getOriginalFilename();
        if (filename.toLowerCase().endsWith(".zip") || "application/zip".equals(multipartFile.getContentType())) {
            TranscriptionZipAction.performOnZipContent(transcriptionId, multipartFile, (fileName, tId, fileContent) -> {
                byte[] content = StreamUtils.copyToByteArray(fileContent);
                TranscriptionPageContentType pageContentType = PageContentTypeRecognizer.recognize(fileName);
                ImportPageRecognizer pageRecognizer =
                        importPageRecognizerFactory.provideImportPageRecognizer(pageContentType);
                Page page = pageRecognizer.recognizePage(tId, new String(content, UTF_8), fileName)
                        .orElseThrow(() -> ZipImportExceptionFactory.unrecognizablePage(fileName));
                page.getPageLayers().stream().filter(pl -> pl.getLayer().getId() == layerId)
                        .forEach(pl -> undoRedoService.reset(pl.getId()));
                pageImportService.addTextLayerToPage(page.getId(), fileName, null,
                        new ByteArrayInputStream(content), layerId, ocrLevel);
            });
        } else {
            if (format == null)
                format = PageContentTypeRecognizer.recognize(filename, multipartFile.getContentType());

            Set<PageLayer> layerPages = layerService.getLayer(layerId).getLayerPages();
            layerPages.forEach(lp -> undoRedoService.reset(lp.getId()));
            try (InputStream inputStream = multipartFile.getInputStream()) {
                pageImportService.addImportedTextLayerToLayer(filename, format.getValue(), inputStream,
                        layerId, transcriptionId, ocrLevel);
            }
        }

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Imported verses from text file to Transcription with id - {} on Layer with id - {}", transcriptionId, layerId);
        LogAction.clearMDCAction();
    }

    private void checkTranscriptionIsUpdatable(Transcription transcription) {
        if (transcription.getCreationMode() != CreationMode.WEB_FORM) {
            throw new ValidationException("Transcription imported from an external service cannot be updated");
        }
    }
}
