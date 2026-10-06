package pl.psnc.vtl.portal.controllers.verses;

import com.fasterxml.jackson.databind.node.TextNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.psnc.vtl.portal.dto.verses.VersesAndBlocksDto;
import pl.psnc.vtl.portal.dto.verses.VersesAndLanguagesDto;
import pl.psnc.vtl.portal.model.verses.*;
import pl.psnc.vtl.portal.services.verses.StudentVerseContentService;
import pl.psnc.vtl.portal.services.verses.VerseContentService;
import pl.psnc.vtl.portal.services.verses.VerseService;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotEmpty;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name= "Verse content", description = "modify contents of verse(s)" )
@Validated
public class VerseContentController {

    @Value("${vtl.verses.payload.max-size-bytes}")
    private long versePayloadMaxSizeInBytes;

    private final VerseContentService verseContentService;
    private final VerseService verseService;

    private final StudentVerseContentService studentVerseContentService;

    @Operation(summary = "Update content of student verse with given verseId")
    @PutMapping(path = "/verses/{verseId}/student-content", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LearningStatus> updateStudentVerseContent(@PathVariable("verseId") Long verseId, @RequestBody(required=false) TextNode content) {
        return ResponseEntity.ok(studentVerseContentService.updateVerseContent(verseId, StringEscapeUtils.unescapeJava(content.asText())));
    }


    /**
     * <p>Updates content of the given verse.</p>
     * <p>This endpoint accepts requests with content type <b>multipart/form-data</b> with the following parts: </p>
     * <li>new text content for verse</li>
     * <li>file as payload for verse (for example xml file with music notation)</li>
     * @param verseId verse identifier
     * @param content new text content for given verse
     */
    @Operation(summary = "Update content of verse with given verseId")
    @PutMapping(path = "/verses/{verseId}/modify-content", consumes = MediaType.APPLICATION_JSON_VALUE)
    public VersesAndBlocksDto updateVerseContent(
            @Parameter(description = "Verse identifier") @PathVariable("verseId") Long verseId,
            @Parameter(description = "Verse text content that will be saved")
            @RequestBody(required = false) TextNode content) {
        return verseService.getVersesAndBlocksDto(
                verseContentService.updateVerseContent(verseId, StringEscapeUtils.unescapeJava(content.asText())));
    }

    /**
     * Uploads file as a payload for the given verse
     * @param verseId verse identifier
     * @param payload file content that should be saved as a payload for verse
     * @throws IOException in case of access errors (if the temporary store fails)
     */
    @Operation(summary = "Uploads payload for given verse")
    @PostMapping(path = "/verses/{verseId}/payload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadVersePayload(
            @Parameter(description = "Verse identifier") @PathVariable("verseId") Long verseId,
            @Parameter(description = "File content to be uploaded") @RequestParam(name = "payload") MultipartFile payload)
            throws IOException {

        if (versePayloadRequestValid(payload)) {
            String fileName = payload.getOriginalFilename();
            if (StringUtils.isBlank(fileName))
                fileName = "payload";
            fileName = fileName.replaceFirst(".*/", "");

            verseContentService.updateVersePayload(verseId, new VersePayload(payload.getBytes(),
                    fileName, payload.getContentType()));
        } else {
            throw new ValidationException("Request invalid. Payload is bigger than allowed.");
        }
    }

    private boolean versePayloadRequestValid(MultipartFile payload) {
        return payload.getSize() < versePayloadMaxSizeInBytes;
    }

    @Operation(summary = "Gets payload for given verse")
    @GetMapping(path = "/verses/{verseId}/payload")
    public ResponseEntity<byte[]> getVersePayload(
            @Parameter(description = "Verse identifier") @PathVariable("verseId") Long verseId) {

        VersePayload payload = verseContentService.readVersePayload(verseId);
        if (payload != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, payload.getPayloadContentType());
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + payload.getPayloadFileName() + "\"");
            return new ResponseEntity<>(payload.getPayload(), headers, HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Deletes payload for given verse")
    @DeleteMapping(path = "/verses/{verseId}/payload")
    @ResponseBody
    public void deleteVersePayload(
            @Parameter(description = "Verse identifier") @PathVariable("verseId") Long verseId) {

        verseContentService.deleteVersePayload(verseId);
    }

    @Operation(summary = "Clear verse formatting")
    @PutMapping(path = "/verses/{verseId}/formatting/clear", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Formatting> clearVerseFormatting(@PathVariable("verseId") Long verseId) {
        return ResponseEntity.ok(verseContentService.clearVerseFormatting(verseId));
    }


    @Operation(summary = "Set verse forme work")
    @PutMapping(path = "/verses/{verseId}/formatting/forme-work/{formeWork}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Formatting> setVerseFormattingFormeWork(@PathVariable("verseId") Long verseId,
            @PathVariable("formeWork") FormeWork formeWork) {
        return ResponseEntity.ok(verseContentService.setVerseFormattingFormeWork(verseId, formeWork));
    }


    @Operation(summary = "Set verse formatting header")
    @PutMapping(path = "/verses/{verseId}/formatting/heading/{heading}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Formatting> setVerseFormattingHeading(@PathVariable("verseId") Long verseId,
            @PathVariable("heading") Heading heading) {
        return ResponseEntity.ok(verseContentService.setVerseFormattingHeading(verseId, heading));
    }


    @PutMapping(path = "/verses/{verseId}/formatting/tabular-form/{tabularForm}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Formatting> setVerseFormattingTabularForm(@PathVariable("verseId") Long verseId,
            @PathVariable("tabularForm") TabularForm tabularForm) {
        return ResponseEntity.ok(verseContentService.setVerseFormattingTabularForm(verseId, tabularForm));
    }


    @Operation(summary = "Set verse formatting end of paragraph")
    @PutMapping(path = "/verses/{verseId}/formatting/end-of-paragraph", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Formatting> setVerseFormattingEndOfParagraph(@PathVariable("verseId") Long verseId) {
        return ResponseEntity.ok(verseContentService.setVerseFormattingEndOfParagraph(verseId));
    }


    @Operation(summary = "Set verse formatting end of list item")
    @PutMapping(path = "/verses/{verseId}/formatting/list-form/{listForm}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Formatting> setVerseFormattingListForm(@PathVariable("verseId") Long verseId,
            @PathVariable("listForm") ListForm listForm) {
        return ResponseEntity.ok(verseContentService.setVerseFormattingListForm(verseId, listForm));
    }

    @Operation(summary = "Indicate that the verse is a caption of an image")
    @PutMapping(path = "/verses/{verseId}/formatting/image-caption", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Formatting> setVerseFormattingImageCaption(@PathVariable("verseId") Long verseId) {
        return ResponseEntity.ok(verseContentService.setVerseFormattingImageCaption(verseId));
    }

    @Operation(summary = "call OCR on multiple verses")
    @PutMapping(path = "/verses/content-recognition", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public VersesAndBlocksDto recognizeVerses(@RequestBody @Valid VersesAndLanguagesDto vl) {
        return verseService
                .getVersesAndBlocksDto(verseContentService.recogniseVerses(vl.getVerseIds(), vl.getLanguageCodes()));
    }

    @Operation(summary = "clear contents of multiple verses")
    @PutMapping(path = "/verses/clear-content", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public VersesAndBlocksDto clearVerses(@RequestBody @NotEmpty List<Long> verseIds) {
        return verseService.getVersesAndBlocksDto(verseContentService.clearVerseContents(verseIds));
    }

    @Operation(summary = "Set verse formatting end of notated-music")
    @PutMapping(path = "/verses/{verseId}/formatting/notated-music", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Formatting> setVerseFormattingEndOfNotatedMusic(@PathVariable("verseId") Long verseId) {
        return ResponseEntity.ok(verseContentService.setVerseFormattingEndOfNotatedMusic(verseId));
    }

    @Operation(summary = "Set verse formatting annotation")
    @PutMapping(path = "/verses/{verseId}/formatting/annotation", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Formatting> setVerseFormattingAnnotation(@PathVariable("verseId") Long verseId) {
        return ResponseEntity.ok(verseContentService.setVerseFormattingAnnotation(verseId));
    }

    @Operation(summary = "Set verse formatting marginalia")
    @PutMapping(path = "/verses/{verseId}/formatting/marginalia", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Formatting> setVerseFormattingMarginalia(@PathVariable("verseId") Long verseId) {
        return ResponseEntity.ok(verseContentService.setVerseFormattingMarginalia(verseId));
    }

    @Operation(summary = "Set verse formatting font - bold")
    @PutMapping(path = "/verses/{verseId}/formatting/font/bold", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Formatting> setVerseFormattingBold(@PathVariable("verseId") Long verseId) {
        return ResponseEntity.ok(verseContentService.setVerseFormattingBold(verseId));
    }

    @Operation(summary = "Set verse formatting font - italic")
    @PutMapping(path = "/verses/{verseId}/formatting/font/italic", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Formatting> setVerseFormattingItalic(@PathVariable("verseId") Long verseId) {
        return ResponseEntity.ok(verseContentService.setVerseFormattingItalic(verseId));
    }

    @Operation(summary = "Set verse formatting font - underline")
    @PutMapping(path = "/verses/{verseId}/formatting/font/underline", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Formatting> setVerseFormattingUnderline(@PathVariable("verseId") Long verseId) {
        return ResponseEntity.ok(verseContentService.setVerseFormattingUnderline(verseId));
    }

    @Operation(summary = "Set verse formatting font - strikethrough")
    @PutMapping(path = "/verses/{verseId}/formatting/font/strikethrough", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Formatting> setVerseFormattingStrikethrough(@PathVariable("verseId") Long verseId) {
        return ResponseEntity.ok(verseContentService.setVerseFormattingStrikethrough(verseId));
    }
}
