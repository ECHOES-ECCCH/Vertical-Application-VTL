package pl.psnc.vtl.portal.services.pages;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import pl.psnc.vtl.portal.conf.logging.LogAction;
import pl.psnc.vtl.portal.model.imports.FileType;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.echoes.EchoesBinaryAsset;
import pl.psnc.vtl.portal.repositories.echoes.EchoesPlatformRepository;
import pl.psnc.vtl.portal.repositories.iiiftools.DownloadFilesResponse;
import pl.psnc.vtl.portal.repositories.iiiftools.IIIFToolsRepository;
import pl.psnc.vtl.portal.repositories.imports.DocumentsLoaderRepository;
import pl.psnc.vtl.portal.services.domains.DomainService;
import pl.psnc.vtl.portal.services.imports.*;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;
import pl.psnc.vtl.portal.services.transcriptions.*;
import pl.psnc.vtl.portal.services.users.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static pl.psnc.vtl.portal.services.registration.LoggedInUserHolder.withLoggedInUser;
import static pl.psnc.vtl.portal.utils.ThreadUtils.withMdc;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentImportService {

    private final TransactionTemplate transactionTemplate;

    private final DomainService domainService;

    private final TranscriptionPermissionService transcriptionPermissionService;

    private final TranscriptionProcessingCoordinator transcriptionProcessingCoordinator;

    private final ModificationTrackingService modificationTrackingService;

    private final PageService pageService;

    private final TranscriptionValidator transcriptionValidator;

    private final UserService userService;

    private final DocumentsLoaderRepository documentsLoaderRepository;

    private final IIIFToolsRepository iiifToolsRepository;

    private final LinkMappersRegistry linkMappersRegistry;

    private final EchoesPlatformRepository echoesPlatformRepository;

    @Value("${vtl.imports-domains: false}")
    private boolean checkImportDomain;

    @Value("${vtl.pagination.pages.default-perpage}")
    private Integer pagesDefaultPerpage;

    private final TranscriptionService transcriptionService;

    @Transactional
    public void importPagesFromIIIF(long transcriptionId, String resourceUrl) throws ConfigurationException {
        if (checkImportDomain) domainService.validateDomain(resourceUrl, FileType.IIIF);

        String manifestString = documentsLoaderRepository.loadDocumentStringFromResource(resourceUrl);
        importPagesFromIIIF(transcriptionId, new LoadedFile(resourceUrl, manifestString));
    }

    @Transactional
    public void importPagesFromEchoesIIIF(long transcriptionId, String assetId) throws ConfigurationException {
        transcriptionPermissionService.checkAdminOrVerifyAccess(transcriptionId);
        LoadedFile loadedFile = echoesPlatformRepository.loadAsset(assetId);
        if (checkImportDomain && StringUtils.isNotBlank(loadedFile.getResourceUrl())) {
            domainService.validateDomain(loadedFile.getResourceUrl(), FileType.IIIF);
        }
        importPagesFromIIIF(transcriptionId, loadedFile);
    }

    public void importPagesFromEchoesFile(long transcriptionId, String assetId) throws IOException {
        transcriptionPermissionService.checkAdminAccess(transcriptionId);
        echoesPlatformRepository.loadBinaryAsset(assetId, file ->
                transcriptionService.addPagesToTranscription(transcriptionId, file));
    }

    private void importPagesFromIIIF(long transcriptionId, LoadedFile loadedFile) {
        Transcription transcription = transcriptionService.requireTranscription(transcriptionId);
        transcriptionPermissionService.checkAdminOrVerifyAccess(transcriptionId);

        LinkMapperStrategy linkMapperStrategy = linkMappersRegistry.getMapperStrategy(FileType.IIIF);
        PublicationLinks externalLinks = linkMapperStrategy.getTranscriptionLinks(loadedFile);

        if (externalLinks.getLinks().isEmpty())
            throw TextFormatValidator.linkLessPublicationException();

        if (!externalLinks.isManifestLinks())
            throw new RuntimeException("Expected manifest links for IIIF");

        userService.validateUsedSpaceQuota(transcription.getCreator());
        transcriptionValidator.validateUsedTranscriptionSpaceQuota(transcriptionId);

        final String OPERATION = "IIIFImport";
        transcriptionProcessingCoordinator.startProcessing(transcriptionId, OPERATION);
        CompletableFuture.runAsync(withMdc(withLoggedInUser(() -> {
            try {
                List<String> urls = externalLinks.getLinks().stream().map(Link::getUrl).collect(Collectors.toList());
                /*
                 * Files saved on IIIF server will not really be used, but downloading them now
                 * checks if they are available and ensures unique file names.
                 */
                // TODO add option to not store downloaded contents, just empty placeholders
                // to save quota?
                DownloadFilesResponse response = iiifToolsRepository.saveTranscriptionFiles(transcriptionId, urls,
                        count -> transcriptionProcessingCoordinator.progress(transcriptionId, OPERATION,
                                100 * count / urls.size()));
                Map<String, String> urlFilesMap = response.getUrlsToFilenames();
                if (urlFilesMap.size() != urls.size()) {
                    throw new RuntimeException(
                            String.format("Failed to save %d transcription files.", urls.size() - urlFilesMap.size()));
                }

                transactionTemplate.executeWithoutResult(status -> {
                    userService.updateUsedSpaceQuota(transcription.getCreator(), response.getQuota());
                    transcriptionService.updateUsedTranscriptionSpaceQuota(response.getQuota(), transcriptionId);

                    pageService.createPagesFromManifest(transcription, externalLinks.getLinks(), urlFilesMap);

                    modificationTrackingService.trackModification(transcription);
                });
                transcriptionProcessingCoordinator.processingFinished(transcriptionId, OPERATION, null);

                LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
                log.info("Imported pages from IIIF object to Transcription with id - {}.", transcriptionId);
                LogAction.clearMDCAction();
            } catch (Exception e) {
                log.error("IIIF import failed for transcription {} from {}", transcriptionId,
                        loadedFile.getResourceUrl(), e);
                transcriptionProcessingCoordinator.processingFinished(transcriptionId, OPERATION, e);
            }
        })));
    }

    /**
     * this method doesn't close the stream
     *
     * @param inputStream
     * @return String
     */
    @SneakyThrows
    private String mapToString(InputStream inputStream) {
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8.toString());
    }

}
