package pl.psnc.vtl.portal.services.imports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;
import pl.psnc.vtl.portal.conf.logging.LogAction;
import pl.psnc.vtl.portal.dto.imports.ImportTranscriptionDTO;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionMetadataCore;
import pl.psnc.vtl.portal.model.imports.FileType;
import pl.psnc.vtl.portal.model.transcriptions.CreationMode;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.transcriptions.TranscriptionExportStatus;
import pl.psnc.vtl.portal.repositories.iiiftools.DownloadFilesResponse;
import pl.psnc.vtl.portal.repositories.iiiftools.IIIFToolsRepository;
import pl.psnc.vtl.portal.repositories.imports.DocumentsLoaderRepository;
import pl.psnc.vtl.portal.services.domains.DomainService;
import pl.psnc.vtl.portal.services.imports.tei.TeiPreservedMetadataExtractor;
import pl.psnc.vtl.portal.services.pages.PageService;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionExportService;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionProcessingCoordinator;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionService;
import pl.psnc.vtl.portal.services.users.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static pl.psnc.vtl.portal.services.registration.LoggedInUserHolder.withLoggedInUser;
import static pl.psnc.vtl.portal.utils.ThreadUtils.withMdc;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportTranscriptionService {

    private final TransactionTemplate transactionTemplate;

    private final IIIFToolsRepository iiifToolsRepository;

    private final DocumentsLoaderRepository documentsLoaderRepository;

    private final LinkMappersRegistry linkMappersRegistry;

    private final MappersRegistry mappersRegistry;

    private final TranscriptionService transcriptionService;

    private final TranscriptionExportService transcriptionExportService;

    private final PageService pageService;

    private final UserService userService;

    private final DomainService domainService;

    private final TranscriptionProcessingCoordinator transcriptionProcessingCoordinator;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${quota.transcription}")
    private Integer translationQuota;

    @Value("${vlt.imports-domains: false}")
    private boolean checkImportDomain;

    public TranscriptionCore importTranscriptionData(ImportTranscriptionDTO importTranscriptionDTO)
            throws ConfigurationException {
        if (checkImportDomain)
            domainService.validateDomain(importTranscriptionDTO);
            
        if (StringUtils.isNotBlank(importTranscriptionDTO.getExternalId())) {
            domainService.validateExternalEditorDomain(importTranscriptionDTO);
        }

        String metadataUrl = StringUtils.defaultIfBlank(
                importTranscriptionDTO.getMetadataUrl(), importTranscriptionDTO.getResourceUrl());
        FileType metadataType = ObjectUtils.defaultIfNull(
                importTranscriptionDTO.getMetadataType(), importTranscriptionDTO.getFileType());
        String contentString = documentsLoaderRepository.loadDocumentStringFromResource(metadataUrl);
        MapperStrategy mapperStrategy = mappersRegistry.getMapperStrategy(metadataType);
        TranscriptionCore transcriptionCore = mapperStrategy.mapToImportTemplate(contentString);
        transcriptionCore.setExternalMetadataUrl(metadataUrl);
        transcriptionCore.setExternalMetadataType(metadataType);

        transcriptionCore.setFileType(importTranscriptionDTO.getFileType());
        if (StringUtils.isNotBlank(importTranscriptionDTO.getMetadataUrl())
                || transcriptionCore.getExternalUrl() == null) {
            transcriptionCore.setExternalUrl(importTranscriptionDTO.getResourceUrl());
        }

        if (StringUtils.isNotBlank(importTranscriptionDTO.getExternalId())) {
            transcriptionCore.setExternalEdit(true);
            transcriptionCore.setExternalId(importTranscriptionDTO.getExternalId());
        } else {
            transcriptionCore.setExternalEdit(importTranscriptionDTO.isExternalEdit());
            if (transcriptionCore.isExternalEdit() && transcriptionCore.getExternalId() == null)
                transcriptionCore.setExternalId(importTranscriptionDTO.getResourceUrl());
        }

        return transcriptionCore;
    }

    @Transactional
    public Transcription createTranscription(TranscriptionCore core, String origin) {
        boolean isMets = core.getFileType() == FileType.METS && core.getExternalMetadataType() == FileType.METS;
        String resourceUrl = isMets ? core.getExternalMetadataUrl() : core.getExternalUrl();
        String resourceString = documentsLoaderRepository.loadDocumentStringFromResource(resourceUrl);
        var linkMapperStrategy = linkMappersRegistry.getMapperStrategy(isMets ? FileType.METS : core.getFileType());
        LoadedFile loadedFile = new LoadedFile(resourceUrl, resourceString);
        var publicationLinks = linkMapperStrategy.getTranscriptionLinks(loadedFile);
        if (publicationLinks.getLinks().isEmpty())
            throw TextFormatValidator.linkLessPublicationException();
        if (core.getFileType() == FileType.TEI) {
            core.setTeiHeader(TeiPreservedMetadataExtractor.getTeiHeader(resourceString));
            core.setTeiBody(TeiPreservedMetadataExtractor.getTeiBody(resourceString));
        }

        Transcription transcription = transcriptionService.createTranscription(core,
                getCreationMode(core.getFileType()));

        userService.validateUsedSpaceQuota(transcription.getCreator());

        applicationEventPublisher.publishEvent(new TranscriptionImportedEvent(transcription, publicationLinks, origin));

        LogAction.setMDCAction(LogAction.CREATE_TRANSCRIPTION);
        log.info("Imported Transcription with id - {}.", transcription.getId());
        LogAction.clearMDCAction();

        return transcription;
    }

    @Transactional
    public void redownloadTranscription(long transcriptionId) {
        Transcription transcription = transcriptionService.requireTranscription(transcriptionId);
        transcriptionExportService.resetTranscriptionExportStatus(transcriptionId);
        
        if (transcription.getExternalUrl() == null) {
            throw new IllegalArgumentException("Transcription " + transcriptionId + " was not imported");
        }

        String metadataUrl = transcription.getExternalMetadataUrl();
        if (metadataUrl == null)
            metadataUrl = transcription.getExternalUrl();
        FileType metadataType = transcription.getExternalMetadataType();
        if (metadataType == null)
            metadataType = FileType.from(transcription.getCreationMode());
        String contentString = documentsLoaderRepository.loadDocumentStringFromResource(metadataUrl);
        MapperStrategy mapperStrategy = mappersRegistry.getMapperStrategy(metadataType);
        TranscriptionMetadataCore metadataCore =
                new TranscriptionMetadataCore(mapperStrategy.mapToImportTemplate(contentString));

        String resourceString =
                documentsLoaderRepository.loadDocumentStringFromResource(transcription.getExternalUrl());
        LinkMapperStrategy linkMapperStrategy =
                linkMappersRegistry.getMapperStrategy(FileType.from(transcription.getCreationMode()));
        LoadedFile loadedFile = new LoadedFile(transcription.getExternalUrl(), resourceString);
        PublicationLinks publicationLinks = linkMapperStrategy.getTranscriptionLinks(loadedFile);

        transcriptionService.updateTranscriptionMetadata(transcriptionId, metadataCore, false);
        pageService.getPages(transcriptionId).forEach(page -> {
            pageService.deletePage(page.getId());
        });
        applicationEventPublisher.publishEvent(new TranscriptionImportedEvent(transcription, publicationLinks, null));
    }

    @TransactionalEventListener
    @Async
    protected void postprocessTranscription(TranscriptionImportedEvent event) {
        long transcriptionId = event.transcription.getId();
        final String OPERATION = "Import";
        transcriptionProcessingCoordinator.startProcessing(transcriptionId, OPERATION);
        CompletableFuture.runAsync(withMdc(withLoggedInUser(() -> {
            try {
                TranscriptionExportStatus expStatus = event.transcription.getExportStatus();
                if (expStatus != null && expStatus != TranscriptionExportStatus.UNDEFINED && event.origin != null)
                    transcriptionExportService.reportTranscriptionStarted(transcriptionId, event.origin);

                List<String> urls =
                        event.publicationLinks.getLinks().stream().map(Link::getUrl).collect(Collectors.toList());

                // This is workaround for indexed djvu files from dlibra
                if (urls.removeIf(x -> x.equals("_index.djvu") || x.equals("index.djvu"))) {
                    log.info("Skipping _index.djvu file import for Transcription with id - {}", transcriptionId);
                }

                DownloadFilesResponse response =
                        iiifToolsRepository.saveTranscriptionFiles(transcriptionId, urls,
                                count -> transcriptionProcessingCoordinator.progress(transcriptionId, OPERATION,
                                        50 * count / urls.size()));
                Map<String, String> urlFilesMap = response.getUrlsToFilenames();
                if (urlFilesMap.size() != urls.size()) {
                    throw new RuntimeException(
                            String.format("Failed to save %d transcription files.", urls.size() - urlFilesMap.size()));
                }

                Map<String, List<String>> fileToPyramidNames = new HashMap<>();
                if (!event.publicationLinks.isManifestLinks()) {
                    fileToPyramidNames.putAll(
                            iiifToolsRepository.preparePyramids(transcriptionId, urlFilesMap.values(),
                                    count -> transcriptionProcessingCoordinator.progress(transcriptionId, OPERATION,
                                            50 + 50 * count / urls.size())));
                } else {
                    transcriptionProcessingCoordinator.progress(transcriptionId, OPERATION, 99);
                }
                transactionTemplate.executeWithoutResult(status -> {
                    userService.updateUsedSpaceQuota(event.transcription.getCreator(), response.getQuota());
                    transcriptionService.updateUsedTranscriptionSpaceQuota(response.getQuota(), transcriptionId);
                    if (event.publicationLinks.isManifestLinks()) {
                        pageService.createPagesFromManifest(event.transcription, event.publicationLinks.getLinks(),
                                urlFilesMap);
                    } else {
                        pageService.createPages(event.transcription, fileToPyramidNames);
                    }
                });
                transcriptionProcessingCoordinator.processingFinished(transcriptionId, OPERATION, null);
            } catch (Exception e) {
                log.error("Import failed for transcription {} from ULR {}", transcriptionId,
                        event.transcription.getExternalUrl(), e);
                transcriptionProcessingCoordinator.processingFinished(transcriptionId, OPERATION, e);
            }
        })));
    }

    private CreationMode getCreationMode(FileType fileType) {
        switch (fileType) {
        case METS:
            return CreationMode.METS;
        case IIIF:
            return CreationMode.IIIF;
        case TEI:
            return CreationMode.TEI;
        case DACE:
            return CreationMode.DACE;
        default:
            return CreationMode.WEB_FORM;
        }
    }

    private static class TranscriptionImportedEvent {
        public final Transcription transcription;
        public final PublicationLinks publicationLinks;
        public final String origin;

        public TranscriptionImportedEvent(Transcription transcription, PublicationLinks publicationLinks,
                String origin) {
            this.transcription = transcription;
            this.publicationLinks = publicationLinks;
            this.origin = origin;
        }
    }
}
