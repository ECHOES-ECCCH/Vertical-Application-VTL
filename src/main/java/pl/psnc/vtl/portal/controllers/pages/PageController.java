package pl.psnc.vtl.portal.controllers.pages;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.psnc.vtl.portal.dto.verses.VersesAndBlocksDto;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.verses.OcrMode;
import pl.psnc.vtl.portal.services.pages.PageContentImportErrorMessages;
import pl.psnc.vtl.portal.services.pages.PageContentImportException;
import pl.psnc.vtl.portal.services.pages.PageImportService;
import pl.psnc.vtl.portal.services.pages.PageService;
import pl.psnc.vtl.portal.services.parsers.ocr.OcrLevel;
import pl.psnc.vtl.portal.services.verses.VerseService;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@RestController
@RequestMapping("/api")
@Tag(name = "Pages")
@RequiredArgsConstructor
public class PageController {


    private final PageService pageService;
    private final PageImportService pageImportService;
    private final VerseService verseService;


    @Operation(summary = "Retrieve the page with layers and blocks")
    @GetMapping(path = "/transcriptions/{transcriptionId}/pages/{pageId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page> getPage(@PathVariable("transcriptionId") long transcriptionId,
            @PathVariable("pageId") long pageId) {
        return ResponseEntity.ok(pageService.getPageWithBlocks(transcriptionId, pageId, null));
    }

    @Operation(summary = "Retrieve pages of a transcription. If fields param is applied and its value is  equal to " +
            "id,page-no(i.e. 'fields=id,page-no') then the list contains only identifiers and page numbers.")
    @GetMapping(path = "/transcriptions/{transcriptionId}/pages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<?>> getPage(@PathVariable("transcriptionId") long transcriptionId,
                                           @RequestParam(value = "fields", required = false, defaultValue = "") List<String> fieldsToRetrieve) {
        if (fieldsToRetrieve.contains("id") && fieldsToRetrieve.contains("page-no") && fieldsToRetrieve.size() == 2) {
            return ResponseEntity.ok(pageService.getPagesIdsAndPageNums(transcriptionId));
        }
        return ResponseEntity.ok(pageService.getPages(transcriptionId));
    }


    @Operation(summary = "Recognize text on the page by running OCR per layer ")
    @PutMapping(path = "/transcriptions/{transcriptionId}/pages/{pageId}/recognize", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VersesAndBlocksDto> recognizePage(@PathVariable("transcriptionId") long transcriptionId,
            @PathVariable("pageId") long pageId,
            @RequestParam String lang, @RequestParam OcrMode ocrMode,
            @RequestParam long layerId,
            @RequestParam(required = false) OcrLevel ocrLevel,
            @RequestParam(required = false, defaultValue = "false") boolean handleColumns) {
        return ResponseEntity.ok(verseService.getVersesAndBlocksDto(
                pageService.recognizePage(transcriptionId, pageId, lang, ocrMode, layerId, ocrLevel, handleColumns)));
    }


//    not supported layers

    /**
     * Returns content of the given page using provided content type. <br/>
     * All allowed content types are listed in {@link TranscriptionPageContentType}
     *
     * @param pageId      identifier of the page that will be used for content generation
     * @param contentType page representation that should be used in generated page content
     * @return page content in the specified format
     */
    @Operation(summary = "Generates and returns page content in the specified format", responses = {
            @ApiResponse(responseCode = "200", description = "Operation finished successfully"),
            @ApiResponse(responseCode = "400", description = "Provided parameter is not correct")})
    @GetMapping(path = "/pages/{pageId}/content")
    public void preparePageContent(
            HttpServletResponse response,
            @Parameter(description = "page identifier") @PathVariable("pageId") long pageId,
            @Parameter(description = "format of the exported file") @RequestParam("type") TranscriptionPageContentType contentType,
            @RequestParam(value = "layerId", required = false) Long layerId
        ) throws IOException {
        List<ImmutablePair<Layer, String>> contents = pageService.getPageContent(pageId, contentType, layerId);
        long transcriptionId = contents.get(0).left.getTranscription().getId();
        Page page = pageService.getPage(transcriptionId, pageId, null);
        String pageNumber = String.format("%04d", page.getPageNo());
        String pageName = FilenameUtils.removeExtension(page.getPath());
        String fileNamePrefix = "t" + transcriptionId + "_p" + pageNumber + "_" + pageName;

        if (contents.size() > 1) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + fileNamePrefix + "_" + contentType + ".zip");
            try (OutputStream out = response.getOutputStream()) {
                pageService.writeZippedContent(contents, pageId, contentType, out);
                out.flush();
            }
        } else {
            byte[] content = contents.get(0).right.getBytes(UTF_8);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(contentType.getContentType());
            response.setCharacterEncoding("UTF-8");
            response.setContentLength(content.length);
            response.setHeader("Content-Disposition",
                    "attachment;filename=\"" + fileNamePrefix + "." + contentType.getExtensions().get(0) + "\"");
            try (OutputStream out = response.getOutputStream()) {
                out.write(content);
                out.flush();
            }
        }
    }


    /**
     * Parses provided plain text or hOCR or TEI with coordinates file and replaces all the verses for the specified {@link Page}
     *
     * @param pageId page identifier. Parsed verses will be added to this page.
     * @param file   file with plain text or hOCR html or TEI with coordinates.
     * @return empty response with the proper status
     */
    @Operation(summary = "Replaces page verses with the ones taken from the specified plain text or hOCR or TEI with coordinates file", responses = {
            @ApiResponse(responseCode = "200", description = "Operation finished successfully")})
    @PostMapping(path = "/pages/{pageId}/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadTextLayerForPage(
            @PathVariable long pageId,
            @RequestBody @RequestParam("file") MultipartFile file,
            @RequestParam Long layerId,
            @RequestParam(required = false) OcrLevel ocrLevel) {

        try (InputStream fileStream = file.getInputStream()) {
            pageImportService.addTextLayerToPage(pageId, file.getOriginalFilename(), file.getContentType(), fileStream,
                    layerId, ocrLevel);
        } catch (IOException e) {
            throw new PageContentImportException(e.getMessage(),
                    PageContentImportErrorMessages.WRONG_FILE.getErrorMessage());
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/pages/{pageId}")
    private ResponseEntity<Void> deletePage(@PathVariable("pageId") long pageId) {
        pageService.deletePage(pageId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Marks the page as empty (no text verses are present).", responses = {
            @ApiResponse(responseCode = "200", description = "Operation finished successfully")})
    @PutMapping(path = "/pages/{pageId}/empty")
    public ResponseEntity<Void> markPageAsEmpty(@PathVariable("pageId") long pageId,
            @RequestParam(defaultValue = "true") boolean empty) {
        pageService.markPageAsEmpty(pageId, empty);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Adds TEI pb (page-beggining) value to given page.", responses = {
            @ApiResponse(responseCode = "200", description = "Operation finished successfully")})
    @PutMapping(path = "/pages/{pageId}/tei_pb_value", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Void> addPagesPBValue(
            @PathVariable("pageId") long pageId,
            @RequestBody(required = false) String teiPBValue
    ) {
        pageService.setTeiPBForPage(pageId, teiPBValue);
        return ResponseEntity.ok().build();
    }
}
