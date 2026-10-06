package pl.psnc.vtl.portal.controllers.events;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.psnc.vtl.portal.dto.events.*;
import pl.psnc.vtl.portal.dto.pages.PaginatedPages;
import pl.psnc.vtl.portal.dto.transcriptions.PaginatedTranscriptions;
import pl.psnc.vtl.portal.model.events.Event;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.services.events.EventService;
import pl.psnc.vtl.portal.services.events.EventService.EventCategory;
import pl.psnc.vtl.portal.services.events.EventService.OnlyMine;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;

import javax.validation.constraints.Min;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@Validated
@RequiredArgsConstructor
@Tag(name = "Events", description = "organize exciting transcription events!")
public class EventController {

    private final EventService eventService;
    private final LoggedInUserHolder loggedInUserHolder;

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Event> createEvent(@RequestBody EventCore newEvent) {
        // access checked in WebSecurityConfig
        Event event = eventService.createEvent(newEvent);
        return ResponseEntity.ok(event);
    }

    @PutMapping(path = "/{eventId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Event> updateEvent(@PathVariable("eventId") Long eventId, @RequestBody EventCore event) {
        eventService.checkWriteAccess(eventId);
        return ResponseEntity.ok(eventService.updateEvent(eventId, event));
    }

    @GetMapping(path = "/{eventId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Event> getEvent(@PathVariable("eventId") Long eventId) {
        eventService.checkReadAccess(eventId);
        Event event = eventService.getEvent(eventId);
        return ResponseEntity.ok(event);
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedEvents> getEvents(
            @RequestParam(required = false) EventCategory category,
            @RequestParam(required = false) OnlyMine onlyMine,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "7") @Min(1) int perpage) {
        return ResponseEntity.ok(eventService.getEvents(category, onlyMine, page, perpage));
    }

    @DeleteMapping(path = "/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("eventId") Long eventId) {
        eventService.checkWriteAccess(eventId);
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{eventId}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> setEventThumbnail(@PathVariable("eventId") Long eventId,
            @RequestBody @RequestParam("file") MultipartFile file) throws IOException {
        eventService.checkWriteAccess(eventId);
        try (InputStream fileStream = file.getInputStream()) {
            eventService.setEventThumbnail(eventId, fileStream, file.getOriginalFilename());
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(path = "/{eventId}/thumbnail")
    public ResponseEntity<Void> deleteEventThumbnail(@PathVariable("eventId") Long eventId) {
        eventService.checkWriteAccess(eventId);
        eventService.deleteEventThumbnail(eventId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = "/{eventId}/attendees", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add attendees to the event", description = "Attendees will be marked as unconfirmed. If user "
            + "wants to add themselves, use the /enroll endpoint.")
    public ResponseEntity<List<EventTeamCore>> addEventAttendees(@PathVariable("eventId") Long eventId,
            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of user e-mails to add "
                    + "as event attendees, don't worry if already added") List<String> emailsToAdd,
            @RequestParam(required = false) Long teamId,
            @RequestHeader("Origin") String origin) {

        eventService.checkWriteAccess(eventId);
        eventService.addAttendees(eventId, emailsToAdd, teamId, origin);
        List<EventTeamCore> res = eventService.getEventTeams(eventId);
        return ResponseEntity.ok(res);
    }

    @PutMapping(path = "/{eventId}/enroll", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add current user to the event")
    public ResponseEntity<Void> enrollCurrentUser(@PathVariable("eventId") Long eventId,
            @RequestHeader("Origin") String origin) {
        eventService.checkReadAccess(eventId);
        eventService.enrollCurrentUser(eventId, origin);
        return ResponseEntity.ok().build();
    }

    @PutMapping(path = "/{eventId}/attendee-confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "User decision on event attendance", description = "Depending on decision, adds "
            + "the current user to the event (if not already added) and confirms their attendance, or "
            + "removes the user from the event (if added).")
    public ResponseEntity<Event> confirmEventAttendee(@PathVariable("eventId") Long eventId,
            @Parameter(description = "true to add/confirm attendance, false to remove from event") boolean attend) {
        eventService.checkReadAccess(eventId);
        if (attend) {
            eventService.confirmAttendee(eventId);
        } else {
            eventService.removeAttendees(eventId, List.of(loggedInUserHolder.getLoggedInUser().getEmail()));
        }
        Event event = eventService.getEvent(eventId);
        return ResponseEntity.ok(event);
    }

    @DeleteMapping(path = "/{eventId}/attendees", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Remove attendees from the event", description = "Event owner and admins can remove other users. "
            + "If a user wants to remove themselves, use the /attendee-confirm endpoint")
    public ResponseEntity<Event> removeEventAttendees(@PathVariable("eventId") Long eventId,
            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of user e-mails to remove "
                    + "as event attendees, don't worry if already not assigned or unknown") List<String> emailsToRemove) {
        eventService.checkWriteAccess(eventId);
        eventService.removeAttendees(eventId, emailsToRemove);
        Event event = eventService.getEvent(eventId);
        return ResponseEntity.ok(event);
    }

    @PutMapping(path = "/{eventId}/teams", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update/replace all teams defined for the event", description = "To update, include team ids. Existing teams that are not included in request will be removed. "
            + "Existing attendees not included in request will be saved as not belonging to any team. "
            + "Users that are not currently attendees will be automatically added as unconfirmed.")
    public ResponseEntity<List<EventTeamCore>> setEventTeams(@PathVariable("eventId") Long eventId,
            @RequestBody List<EventTeamCore> teams) {
        eventService.checkWriteAccess(eventId);
        List<EventTeamCore> res = eventService.setEventTeams(eventId, teams);
        return ResponseEntity.ok(res);
    }

    @GetMapping(path = "/{eventId}/teams", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EventTeamCore>> getEventTeams(@PathVariable("eventId") Long eventId) {
        eventService.checkReadAccess(eventId);
        List<EventTeamCore> res = eventService.getEventTeams(eventId);
        return ResponseEntity.ok(res);
    }

    @PutMapping(path = "/{eventId}/transcriptions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedTranscriptions<Transcription>> addEventTranscriptions(@PathVariable("eventId") Long eventId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "7") @Min(1) int perpage,
            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of transcription ids to add, "
                    + "don't worry if already added") List<Long> transcriptionsToAdd) {
        eventService.checkWriteAccess(eventId);
        eventService.addEventTranscriptions(eventId, transcriptionsToAdd);
        PaginatedTranscriptions<Transcription> transcriptions = eventService.getEventTranscriptions(eventId, page, perpage);
        return ResponseEntity.ok(transcriptions);
    }

    @DeleteMapping(path = "/{eventId}/transcriptions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedTranscriptions<Transcription>> removeEventTranscriptions(@PathVariable("eventId") Long eventId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "7") @Min(1) int perpage,
            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of transcription ids to remove, "
                    + "don't worry if already not assigned or unknown") List<Long> transcriptionsToAdd) {
        eventService.checkWriteAccess(eventId);
        eventService.removeEventTranscriptions(eventId, transcriptionsToAdd);
        PaginatedTranscriptions<Transcription> transcriptions = eventService.getEventTranscriptions(eventId, page, perpage);
        return ResponseEntity.ok(transcriptions);
    }

    @GetMapping(path = "/{eventId}/transcriptions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedTranscriptions<Transcription>> getEventTrancriptions(@PathVariable("eventId") Long eventId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "7") @Min(1) int perpage) {
        eventService.checkReadAccess(eventId);
        PaginatedTranscriptions<Transcription> transcriptions = eventService.getEventTranscriptions(eventId, page, perpage);
        return ResponseEntity.ok(transcriptions);
    }

    @PutMapping(path = "/{eventId}/teams/{teamId}/pages", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addTeamPages(@PathVariable long eventId,
            @PathVariable long teamId,
            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of page ids to add, "
                    + "don't worry if already added.") List<Long> pagesToAdd) {
        eventService.checkWriteAccess(eventId);
        eventService.addTeamPages(eventId, teamId, pagesToAdd);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(path = "/{eventId}/teams/{teamId}/pages", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> removeTeamPages(@PathVariable long eventId,
            @PathVariable long teamId,
            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of page ids to remove, "
                    + "don't worry if already not assigned or unknown.") List<Long> pagesToRemove) {
        eventService.checkWriteAccess(eventId);
        eventService.removeTeamPages(eventId, teamId, pagesToRemove);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/{eventId}/transcriptions/{transcriptionId}/pages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedPages> getTeamPages(
            @PathVariable long eventId,
            @PathVariable long transcriptionId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(defaultValue = "true") boolean includeUnassigned,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "24") @Min(1) int perpage) {
        eventService.checkReadAccess(eventId);
        PaginatedPages pages = eventService.getEventTranscriptionPages(eventId, transcriptionId, teamId,
                includeUnassigned, page, perpage);
        return ResponseEntity.ok(pages);
    }

    @PostMapping(path = "/{eventId}/messages", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Send message to all event attendees", description = "")
    public ResponseEntity<Void> sendMessageToAttendees(
            @Parameter(description = "Event identifier") @PathVariable("eventId") Long eventId,
            @Parameter(description = "Message title") @RequestParam String title,
            @Parameter(description = "Message content") @RequestParam String content,
            @RequestHeader("Origin") String origin
    ) {
        eventService.checkWriteAccess(eventId, true);
        eventService.sendMessagesToAttendees(eventId, title, content, origin);
        return ResponseEntity.ok().build();
    }
}
