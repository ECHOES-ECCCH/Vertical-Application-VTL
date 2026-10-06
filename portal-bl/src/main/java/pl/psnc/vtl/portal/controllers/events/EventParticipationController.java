package pl.psnc.vtl.portal.controllers.events;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.pages.projections.PageIdAndPageNo;
import pl.psnc.vtl.portal.services.events.EventParticipationService;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@Validated
@RequiredArgsConstructor
@Tag(name = "EventsParticipation", description = "Where normal viewer uses transcription, eventViewer "
        + "uses event and this controller translates between them")
public class EventParticipationController {

    private final EventParticipationService eventParticipationService;

    @GetMapping(path = "/{eventId}/pages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PageIdAndPageNo>> getPage(@PathVariable long eventId,
            @RequestParam(value = "fields", required = false, defaultValue = "") List<String> fieldsToRetrieve) {
        if (fieldsToRetrieve.contains("id") && fieldsToRetrieve.contains("page-no") && fieldsToRetrieve.size() == 2) {
            return ResponseEntity.ok(eventParticipationService.getPagesIdsAndPageNums(eventId));
        }
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Operation(summary = "Retrieve the page with layers and blocks")
    @GetMapping(path = "/{eventId}/pages/{pageId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page> getPage(@PathVariable long eventId, @PathVariable long pageId) {
        return ResponseEntity.ok(eventParticipationService.getPageWithBlocks(eventId, pageId));
    }

    @Operation(summary = "Request IIIF manifest for the given page", tags = "IIIF")
    @GetMapping(path = "/{eventId}/iiif/pages/{pageId}/info.json")
    public ResponseEntity<String> infoJsonProvider(@PathVariable long eventId, @PathVariable long pageId) {
        String infoJson = eventParticipationService.infoJsonProvider(eventId, pageId);
        return ResponseEntity.ok(infoJson);
    }

    @GetMapping(path = "/{eventId}/iiif/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> searchTranscriptions(@PathVariable long eventId,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(eventParticipationService.getSearchResults(eventId, q));
    }
}
