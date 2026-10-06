package pl.psnc.vtl.portal.controllers.transcriptions;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.dto.indexes.IndexCore;
import pl.psnc.vtl.portal.dto.indexes.IndexDto;
import pl.psnc.vtl.portal.dto.layers.LayerCore;
import pl.psnc.vtl.portal.dto.layers.LayerCreationDto;
import pl.psnc.vtl.portal.dto.layers.LayerDto;
import pl.psnc.vtl.portal.dto.transcriptions.*;
import pl.psnc.vtl.portal.dto.transcriptions.list.TranscriptionInfoRegularDto;
import pl.psnc.vtl.portal.dto.transcriptions.list.TranscriptionInfoUsersDto;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.transcriptions.CreationMode;
import pl.psnc.vtl.portal.model.transcriptions.Hidden;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.transcriptions.TranscriptionInfo;
import pl.psnc.vtl.portal.model.verses.OcrMode;
import pl.psnc.vtl.portal.services.eBook.AsyncTaskResponse;
import pl.psnc.vtl.portal.services.layers.LayerCreationService;
import pl.psnc.vtl.portal.services.layers.LayerService;
import pl.psnc.vtl.portal.services.pages.WrongFileTypeException;
import pl.psnc.vtl.portal.services.parsers.ocr.OcrLevel;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionContentGeneratorService;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionListService;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionProcessingCoordinator;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionService;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;
import javax.validation.constraints.Min;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@RestController
@RequestMapping("/api")
@Validated
@RequiredArgsConstructor
@Tag(name = "Transcription")
public class TranscriptionController {

    private final TranscriptionService transcriptionService;
    private final LayerService layerService;
    private final LayerCreationService layerCreationService;
    private final TranscriptionListService transcriptionListService;
    private final TranscriptionContentGeneratorService transcriptionContentGeneratorService;
    private final TranscriptionProcessingCoordinator transcriptionProcessingCoordinator;
    @Value("${vtl.pagination.transcriptions.default-perpage}")
    private Integer transcriptionsDefualtPerpage;
    @Value("${vtl.pagination.transcriptions.maximal-perpage}")
    private Integer transcriptionsMaximalPerpage;
    @Value("${vtl.pagination.pages.default-perpage}")
    private Integer pagesDefaultPerpage;
    @Value("${vtl.pagination.pages.maximal-perpage}")
    private Integer pagesMaximalPerpage;

    @GetMapping(path = "/transcriptions/{transcriptionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TranscriptionRegularDto> getTranscription(
            @PathVariable("transcriptionId") Long transcriptionId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "perpage", required = false) Integer perpage) {
        perpage = getPagesDefaultPerpage(perpage, pagesDefaultPerpage, pagesMaximalPerpage);
        page = (page == null) ? 1 : page;
        return ResponseEntity.ok(transcriptionService.getTranscription(transcriptionId, page, perpage));
    }

    @GetMapping(path = "/transcriptions/{transcriptionId}/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lists all the permissions for the given transcription", responses = {
            @ApiResponse(responseCode = "200", description = "Returns list of all permissions")
    })
    public ResponseEntity<TranscriptionPermissions> getTranscriptionPermissions(
            @PathVariable("transcriptionId") Long transcriptionId) {
        return ResponseEntity.ok(transcriptionService.getTranscriptionPermissions(transcriptionId));
    }

    @GetMapping(path = "/transcriptions", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Returns a given page of a sorted list of transcriptions matching the query. For each transcription a list of its initial pages is returned.")
    public ResponseEntity<PaginatedTranscriptions<Transcription>> getTranscriptions(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "all") SearchType type,
            @RequestParam(defaultValue = "all") SearchScope scope,
            @RequestParam(defaultValue = "all") SearchVisibility visibility,
            @RequestParam(defaultValue = "last_modification_date") SearchOrder order,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) Integer perpage) {
        perpage = getPagesDefaultPerpage(perpage, transcriptionsDefualtPerpage, transcriptionsMaximalPerpage);
        return ResponseEntity
                .ok(transcriptionListService.getTranscriptions(q, type, scope, visibility, order, null, page, perpage));
    }

    @GetMapping(path = "/transcriptions/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Returns a given page of a sorted list of transcriptions matching the query. " +
            "Sort direction is derived from the order's default direction or can be imposed explicitly " +
            "in the order-dir parameter. For each transcription the following is provided: " +
            "title (from analytics info), owner's e-mail and quota related info (each with respective id if applicable).")
    public ResponseEntity<PaginatedTranscriptions<TranscriptionInfoRegularDto>> getTranscriptionsList(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "all") SearchType type,
            @RequestParam(defaultValue = "all") SearchScope scope,
            @RequestParam(defaultValue = "all") SearchVisibility visibility,
            @RequestParam(defaultValue = "last_modification_date") SearchOrder order,
            @RequestParam(required = false, name = "order-dir") Sort.Direction orderDir,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) Integer perpage) {
        perpage = getPagesDefaultPerpage(perpage, transcriptionsDefualtPerpage, transcriptionsMaximalPerpage);
        return ResponseEntity.ok(transcriptionListService.getTranscriptionsInfo(q, type, scope, visibility, order,
                orderDir, page, perpage));
    }

    @GetMapping(path = "/transcriptions/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Returns a given page of a sorted list of transcriptions matching the Full Text Search query.")
    public ResponseEntity<PaginatedTranscriptions<TranscriptionSearchResult>> searchTranscriptions(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "all") SearchType type,
            @RequestParam(defaultValue = "all") SearchScope scope,
            @RequestParam(defaultValue = "all") SearchVisibility visibility,
            @RequestParam(defaultValue = "last_modification_date") SearchOrder order,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) Integer perpage) {
        perpage = getPagesDefaultPerpage(perpage, transcriptionsDefualtPerpage, transcriptionsMaximalPerpage);
        return ResponseEntity.ok(
                transcriptionListService.searchTranscriptions(q, type, null, scope, visibility, order, page, perpage));
    }


    @GetMapping(path = "/my-transcriptions", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Returns a given page of a sorted list of logged in user's transcriptions matching the query. For each transcription a list of its initial pages is returned.")
    public ResponseEntity<PaginatedTranscriptions<Transcription>> getMyTranscriptions(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "all") SearchType type,
            @RequestParam(defaultValue = "creator_or_member") SearchScope scope,
            @RequestParam(defaultValue = "all") SearchVisibility visibility,
            @RequestParam(defaultValue = "last_modification_date") SearchOrder order,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) Integer perpage) {
        perpage = getPagesDefaultPerpage(perpage, transcriptionsDefualtPerpage, transcriptionsMaximalPerpage);
        return ResponseEntity
                .ok(transcriptionListService.getTranscriptions(q, type, scope, visibility, order, null, page, perpage));
    }


    @GetMapping(path = "/user-transcriptions", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Returns a given page of a list of transcriptions owned (created) by a given user. No initial pages are returned.")
    public ResponseEntity<PaginatedTranscriptionsWithOwner<Transcription>> getUserTranscriptions(
            @RequestParam(required = false) String q,
            @RequestParam Long ownerId,
            @RequestParam(defaultValue = "last_modification_date") SearchOrder order,
            @RequestParam(required = false, name = "order-dir") Sort.Direction orderDir,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) Integer perpage) {
        perpage = getPagesDefaultPerpage(perpage, transcriptionsDefualtPerpage, transcriptionsMaximalPerpage);
        return ResponseEntity.ok(transcriptionListService.getUserTranscriptions(ownerId, page, perpage, q, order, orderDir));
    }

    @GetMapping(path = "/user-transcriptions/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Returns a given page of a list of transcriptions owned (created) by a given user. " +
            "Sort direction is derived from the order's default direction or can be imposed explicitly " +
            "in the order-dir parameter." +
            "For each transcription the following information is returned: id of transcription, title (from analytic info), " +
            "list of users (each with email and its id).")
    public ResponseEntity<PaginatedTranscriptionsWithOwner<TranscriptionInfoUsersDto>> getUserTranscriptionsList(
            @RequestParam(required = false) String q,
            @RequestParam Long ownerId,
            @RequestParam(defaultValue = "last_modification_date") SearchOrder order,
            @RequestParam(required = false, name = "order-dir") Sort.Direction orderDir,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) Integer perpage) {
        perpage = getPagesDefaultPerpage(perpage, transcriptionsDefualtPerpage, transcriptionsMaximalPerpage);
        return ResponseEntity.ok(transcriptionListService.getUserTranscriptionsInfo(ownerId, page, perpage, q, order, orderDir));
    }


    @PostMapping(path = "/transcriptions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Transcription> createTranscription(@RequestBody TranscriptionCore newTranscription) {
        return ResponseEntity.ok(transcriptionService.createTranscription(newTranscription, CreationMode.WEB_FORM));
    }


    @PutMapping(path = "/transcriptions/{transcriptionId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Transcription> updateTranscription(@PathVariable("transcriptionId") Long transcriptionId,
                                                             @RequestBody TranscriptionCore newTranscription) {
        return ResponseEntity.ok(transcriptionService.updateTranscription(transcriptionId, newTranscription));
    }

    @PutMapping(path = "/transcriptions/{transcriptionId}/metadata", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TranscriptionMetadataCore> putTranscriptionMetadata(
            @PathVariable("transcriptionId") Long transcriptionId,
            @RequestBody TranscriptionMetadataCore transcriptionMetadataCore) {
        return ResponseEntity.ok(
                transcriptionService.updateTranscriptionMetadata(transcriptionId, transcriptionMetadataCore, true));
    }

    @GetMapping(path = "/transcriptions/{transcriptionId}/summarize", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Return transcription action statistics")
    public ResponseEntity<List<TranscriptionSummarize>> getTranscriptionSummarize(
            @PathVariable("transcriptionId") Long transcriptionId) {
        return ResponseEntity.ok(transcriptionService.summarizeTranscription(transcriptionId));
    }

    @PutMapping(path = "/transcriptions/{transcriptionId}/info", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TranscriptionInfo> putTranscriptionInfo(
            @PathVariable("transcriptionId") Long transcriptionId,
            @RequestBody TranscriptionInfoCore transcriptionInfo) {
        return ResponseEntity.ok(transcriptionService.updateTranscriptionInfo(transcriptionId, transcriptionInfo));
    }

    @PostMapping(path = "/transcriptions/{transcriptionId}/add-pages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addPagesToTranscription(
            @PathVariable("transcriptionId") Long transcriptionId,
            @RequestBody @RequestParam("file") MultipartFile file)
            throws IOException, WrongFileTypeException {
        transcriptionService.addPagesToTranscription(transcriptionId, file);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/transcriptions/{transcriptionId}")
    public ResponseEntity<Void> deleteTranscriptionById(@PathVariable("transcriptionId") Long transcriptionId) {
        transcriptionService.deleteTranscription(transcriptionId);
        return ResponseEntity.ok().build();
    }


    /**
     * Rest API call that will change the 'hidden' flag (@{@link Transcription#isHidden()}) of the given transcription to the value specified by the caller
     *
     * @param transcriptionId transcription identifier that will be used to retrieve proper transcription
     * @param flagValue       value true/false that will be set as a transcription's 'hidden' field value
     * @return transcription description or empty response in case of not found transcription
     */
    @PutMapping(path = "/transcriptions/{transcriptionId}/hidden", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Changes 'hidden' flag of the given transcription to the value specified be the user in the request",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation finished successfully")
            })
    public ResponseEntity<Transcription> changeTranscriptionHiddenValue(
            @PathVariable("transcriptionId") Long transcriptionId,
            @RequestBody Hidden flagValue) {
        return ResponseEntity.ok(transcriptionService.changeTranscriptionVisibility(transcriptionId, flagValue.getHidden()));
    }

    @Operation(summary = "Getting transcription default language")
    @GetMapping(path = "/transcriptions/{transcriptionId}/default-language", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTranscriptionDefaultLanguage(
            @PathVariable("transcriptionId") Long transcriptionId) {
        return ResponseEntity.ok(transcriptionService.getTranscriptionDefaultLanguage(transcriptionId));
    }

    /**
     * Returns content of the given transcription using provided content type. <br/>
     * Content of the all transcription pages will be used here. <br/>
     * All allowed content types are listed in {@link TranscriptionPageContentType}
     */
    @Operation(summary = "Export transcription project with layers. Generates and returns content of all pages "
            + "in given transcription in the specified format")
    @GetMapping(path = "/transcriptions/{transcriptionId}/export/content")
    public void prepareTranscriptionContent(
            HttpServletResponse response,
            @PathVariable long transcriptionId,
            @RequestParam TranscriptionPageContentType type,
            @Parameter(description = "Layer ID or empty to get all layers (one file per layer)") @RequestParam(required = false) Long layerId,
            @Parameter(description = "If true (default), prepare zip with one file per page, otherwise direct file with whole layer. Only relevant if layerId is provided") @RequestParam(defaultValue = "true") boolean oneFilePerPage
    ) throws IOException {
        var props = TranscriptionGenerationProperties.builder().transcriptionId(transcriptionId).contentType(type)
                .layerId(layerId).build();
        if (oneFilePerPage || layerId == null) {
            String fileName = "t_" + transcriptionId + "." + type.toString() + ".zip";
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            try (OutputStream out = response.getOutputStream()) {
                transcriptionContentGeneratorService.prepareTranscriptionContent(props, out);
                out.flush();
            }
        } else {
            Transcription transcription = transcriptionService.requireTranscription(transcriptionId);
            Layer layer = layerService.getLayer(layerId);
            byte[] content =
                    transcriptionContentGeneratorService.prepareSingleFile(type, transcription, layer).getBytes(UTF_8);
            String fileName =
                    "t_" + transcriptionId + "." + TranscriptionContentGeneratorService.prepareLayerName(layer, type);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(type.getContentType());
            response.setCharacterEncoding(UTF_8.name());
            response.setContentLength(content.length);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            try (OutputStream out = response.getOutputStream()) {
                out.write(content);
                out.flush();
            }
        }
    }

    private Integer getPagesDefaultPerpage(Integer perpage, Integer defualtPerpage, Integer maximalPerpage) {
        perpage = (perpage == null) ? defualtPerpage : perpage;
        if (perpage > maximalPerpage) {
            throw new PageTooLargeException(maximalPerpage);
        }
        return perpage;
    }

    @Operation(summary = "Execute recognition of all or selected pages in the transcription per layer")
    @PutMapping(path = "/transcriptions/{transcriptionId}/ocr", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> recognizeTranscription(
            @Parameter(name = "transcriptionId", description = "Identifier of the transcription to recognize.")
            @PathVariable("transcriptionId") long transcriptionId,
            @Parameter(name = "ocrMode", description = "<p><span style=\"color: #000000;\">OCR mode determines how the recognized text will be used for the given transcription.<br /></span></p>\n" +
                    "<p><span style=\"color: #000000;\"><strong>OVERRIDE</strong> - remove all verses from the page and store the newly recognized<br /></span></p>\n" +
                    "<p><span style=\"color: #000000;\"><strong>UPDATE</strong> - update with newly recognized text only verses that have no content<br /></span></p>\n" +
                    "<p><span style=\"color: #000000;\"><strong>EMPTYONLY</strong> - recognize only pages that have no verses<br /></span></p>")
            @RequestParam OcrMode ocrMode,
            @Parameter(name = "lang", description = "Language of the text used for recognition (ISO 639-3 3 letter code), "
                    + "can be a list of languages separated by plus sign")
            @RequestParam String lang,
            @Parameter(name = "pages", description = "Pages selected for recognition. When not supplied all pages of the selected transcription will be used." +
                    "Page numbers should be separated with a comma. Page ranges are also possible. Examples: \"1,2,7\", \"1-4\", \"1,5,8-10\"")
            @RequestParam(required = false) String pages,
            @Parameter(name = "layerId", description = "Id of layer on which OCR process should return result")
            @RequestParam Long layerId,
            @RequestParam(required = false) OcrLevel ocrLevel,
            @RequestParam(required = false, defaultValue = "false") boolean handleColumns) {

        transcriptionService.recognizeTranscriptionPages(transcriptionId, ocrMode, lang, pages, layerId, ocrLevel,
                handleColumns);
        return ResponseEntity.created(URI.create("/api/transcriptions/" + transcriptionId + "/processing-status"))
                .build();
    }

    @GetMapping("/transcriptions/{transcriptionId}/processing-status")
    public ResponseEntity<AsyncTaskResponse> getOcredTranscriptionStatus(
            @PathVariable("transcriptionId") long transcriptionId) {
        return ResponseEntity.ok(transcriptionProcessingCoordinator.getProcessing(transcriptionId));
    }

    @Operation(summary = "Acknowledges the status of processing so that it's not returned again")
    @PutMapping("/transcriptions/{transcriptionId}/processing-status-acknowledge")
    public ResponseEntity<Void> acknowledgeOcredTranscriptionStatus(
            @PathVariable("transcriptionId") long transcriptionId) {
        transcriptionProcessingCoordinator.acknowledgeStatus(transcriptionId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create index for transcription ")
    @PostMapping(path = "/transcriptions/{transcriptionId}/indexes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IndexDto> createIndexForTranscription(
            @PathVariable("transcriptionId") long transcriptionId,
            @RequestBody IndexCore indexCore) {
        return ResponseEntity.ok(transcriptionService.createIndex(transcriptionId, indexCore));
    }

    @Operation(summary = "Retrieve all indexes for all pages for given transcription ")
    @GetMapping(path = "/transcriptions/{transcriptionId}/indexes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<IndexDto>> getIndexesForTranscription(
            @PathVariable("transcriptionId") long transcriptionId) {
        return ResponseEntity.ok(transcriptionService.getAllIndexesForTranscription(transcriptionId));
    }


    @Operation(summary = "Search for indexes per transcription")
    @GetMapping(path = "/transcriptions/{transcriptionId}/indexes/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<IndexDto>> searchIndexes(@PathVariable("transcriptionId") long transcriptionId,
                                                        @RequestParam(value = "searchPhrase") String searchPhrase,
                                                        @RequestParam(value = "type", required = false) String type) {
        return ResponseEntity.ok(transcriptionService.searchIndexes(transcriptionId, searchPhrase, type));
    }


    @Operation(summary = "Create layer for given transcription")
    @PostMapping(path = "/transcriptions/{transcriptionId}/layer", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LayerDto> createLayer(@PathVariable("transcriptionId") Long transcriptionId,
                                                @RequestBody LayerCreationDto layerCreationDto) {
        if (requestIsValid(layerCreationDto)) {
            return ResponseEntity.ok(layerCreationService.createLayer(transcriptionId, layerCreationDto));
        } else throw new ValidationException("Provided values are not valid! Copying layer is not allowed for layers " +
                "with non-default type.");
    }

    private boolean requestIsValid(LayerCreationDto layerCreationDto) {
        if (layerCreationDto.getType() != null) {
            return layerCreationDto.getCopy() == LayerCreationDto.COPY.NONE;
        }
        return true;
    }

    @Operation(summary = "Update layer for given transcription. NOTE: layer type is ignored, it can't be changed")
    @PutMapping(path = "/transcriptions/{transcriptionId}/layer/{layerId}")
    public ResponseEntity<LayerDto> updateLayer(@PathVariable("transcriptionId") Long transcriptionId,
                                                @PathVariable("layerId") Long layerId,
                                                @RequestBody LayerCore layerCore) {

        return ResponseEntity.ok(transcriptionService.updateLayer(transcriptionId, layerId, layerCore));
    }


    @Operation(summary = "Delete layer for given transcription")
    @DeleteMapping(path = "/transcriptions/{transcriptionId}/layer/{layerId}")
    public ResponseEntity<Void> deleteLayer(@PathVariable("transcriptionId") Long transcriptionId,
                                            @PathVariable("layerId") Long layerId) {

        transcriptionService.deleteLayer(transcriptionId, layerId);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Get all layers for given transcription")
    @GetMapping(path = "/transcriptions/{transcriptionId}/layer", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LayerDto>> getLayersPerTranscription(
            @PathVariable("transcriptionId") Long transcriptionId) {

        return ResponseEntity.ok(transcriptionService.getAllLayersPerTranscription(transcriptionId));
    }

    //Get single layer
    @Operation(summary = "Get all layers for given transcription")
    @GetMapping(path = "/transcriptions/{transcriptionId}/layer/{layerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LayerDto> getLayers(@PathVariable("transcriptionId") Long transcriptionId,
                                              @PathVariable("layerId") Long layerId) {

        return ResponseEntity.ok(transcriptionService.getLayer(transcriptionId, layerId));
    }

    @Operation(summary = "Update transcription quota")
    @PutMapping(path = "/transcriptions/{transcriptionId}/quota")
    public ResponseEntity<Void> updateTranscriptionQuota(@PathVariable long transcriptionId,
                                                         @RequestParam(value = "quota") Integer quota) {
        transcriptionService.updateTranscriptionSpaceQuota(quota, transcriptionId);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Import data to layer for given transcription for TEI and HOCR")
    @PutMapping(path = "/transcriptions/{transcriptionId}/layer/{layerId}/import")
    public ResponseEntity<Void> importToLayer(@PathVariable("transcriptionId") Long transcriptionId,
            @PathVariable("layerId") Long layerId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) TranscriptionPageContentType format,
            @RequestParam(required = false) OcrLevel ocrLevel)
            throws Exception {

        transcriptionService.importIntoLayer(transcriptionId, layerId, format, file, ocrLevel);
        return ResponseEntity.ok().build();
    }
}
