package pl.psnc.vtl.portal.controllers.verses;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.dto.regions.BoundingBoxCore;
import pl.psnc.vtl.portal.dto.verses.VersesAndBlocksDto;
import pl.psnc.vtl.portal.model.layers.PageLayer.Id;
import pl.psnc.vtl.portal.model.verses.VerseStatus;
import pl.psnc.vtl.portal.services.progress.ProgressService;
import pl.psnc.vtl.portal.services.verses.IncorrectPositionException;
import pl.psnc.vtl.portal.services.verses.VerseService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Verses", description = "All operations related to verses but not to their content. ")
public class VerseController {

    private final VerseService verseService;
    private final ProgressService progressService;

    @Operation(summary = "Add verse to the page with given pageId and layerId")
    @PostMapping(path = "/verses/{pageId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VersesAndBlocksDto> addVerse(@PathVariable("pageId") Long pageId,
                                                       @RequestBody
                                                @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Definition of newly created verse")
                                                VerseCreationDto verseCreationDto,
                                                       @RequestParam(required = true) Long layerId) {
        Id pageLayerId = verseService.addVerse(pageId, verseCreationDto, layerId);
        progressService.updatePageProgress(pageId);
        VersesAndBlocksDto result = verseService.getVersesAndBlocksDto(pageLayerId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Update coordinates of verse with given verseId")
    @PutMapping(path = "/verses/{verseId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateVerseCoordinates(@PathVariable("verseId") Long verseId,
                                       @RequestBody BoundingBoxCore boundingBox) {
        verseService.updateVerseCoordinates(verseId, boundingBox);
    }


    @Operation(summary = "Move  given verse up")
    @PutMapping(path = "/verses/{verseId}/move-up", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VersesAndBlocksDto> moveVerseUp(
            @PathVariable("verseId") Long verseId,
            @RequestParam(defaultValue = "1") int times)
            throws IncorrectPositionException {
        VersesAndBlocksDto result =
                verseService.getVersesAndBlocksDto(verseService.moveVersesUp(List.of(verseId), times));
        return ResponseEntity.ok(result);
    }


    @Operation(summary = "Move  given verse down")
    @PutMapping(path = "/verses/{verseId}/move-down", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VersesAndBlocksDto> moveVerseDown(
            @PathVariable("verseId") Long verseId,
            @RequestParam(defaultValue = "1") int times)
            throws IncorrectPositionException {
        VersesAndBlocksDto result =
                verseService.getVersesAndBlocksDto(verseService.moveVersesDown(List.of(verseId), times));
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Move given verses up")
    @PutMapping(path = "/verses/move-up", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VersesAndBlocksDto> moveVersesUp(
            @RequestBody List<Long> versesIds,
            @RequestParam(defaultValue = "1") int times)
            throws IncorrectPositionException {
        VersesAndBlocksDto result = verseService.getVersesAndBlocksDto(verseService.moveVersesUp(versesIds, times));
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Move  given verse down")
    @PutMapping(path = "/verses/move-down", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VersesAndBlocksDto> moveVersesDown(
            @RequestBody List<Long> versesIds,
            @RequestParam(defaultValue = "1") int times)
            throws IncorrectPositionException {
        VersesAndBlocksDto result = verseService.getVersesAndBlocksDto(verseService.moveVersesDown(versesIds, times));
        return ResponseEntity.ok(result);
    }


    @Operation(summary = "Indicate that the verse is an image")
    @PutMapping(path = "/verses/{verseId}/image")
    public void setVerseAsImage(@PathVariable("verseId") Long verseId) {
        verseService.setVerseAsImage(verseId);
    }

    @Operation(summary = "Set status of given verses")
    @PutMapping(path = "/verses/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VersesAndBlocksDto> changeVersesStatus(
            @RequestBody List<Long> versesIds,
            @RequestParam VerseStatus newStatus)
            throws IncorrectPositionException {
        VersesAndBlocksDto result = verseService.changeVersesStatus(versesIds, newStatus);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Delete verse")
    @DeleteMapping("/verses/{verseId}")
    public ResponseEntity<VersesAndBlocksDto> deleteVerse(@PathVariable("verseId") long verseId) {
        Id pageLayerId = verseService.deleteVerses(List.of(verseId));
        progressService.updatePageProgress(pageLayerId.getPageId());
        VersesAndBlocksDto result = verseService.getVersesAndBlocksDto(pageLayerId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Delete many verses by verseNo")
    @DeleteMapping(path = "/verses", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VersesAndBlocksDto> deleteVerses(
            @RequestBody List<Long> versesIds) {
        Id pageLayerId = verseService.deleteVerses(versesIds);
        progressService.updatePageProgress(pageLayerId.getPageId());
        VersesAndBlocksDto result = verseService.getVersesAndBlocksDto(pageLayerId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "change status of all verses on given page and layer to given status")
    @PutMapping(path = "/verses/{pageId}/change-all-status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VersesAndBlocksDto> changeAllVerseOnPageLayerStatus(
            @PathVariable("pageId") Long pageId,
            @RequestParam Long layerId,
            @RequestParam VerseStatus status) {
        return ResponseEntity.ok(verseService.changeAllVerseOnPageLayerStatus(pageId, layerId, status));
    }
}
