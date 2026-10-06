package pl.psnc.vtl.portal.services.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.BeanPropertyBindingResult;
import pl.psnc.vtl.portal.conf.logging.LogAction;
import pl.psnc.vtl.portal.controllers.ErrorResponseMessage;
import pl.psnc.vtl.portal.controllers.RequestFieldsValidationException;
import pl.psnc.vtl.portal.dto.events.*;
import pl.psnc.vtl.portal.dto.events.EventTeamCore.Member;
import pl.psnc.vtl.portal.dto.pages.PaginatedPages;
import pl.psnc.vtl.portal.dto.transcriptions.PaginatedTranscriptions;
import pl.psnc.vtl.portal.model.events.*;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.users.SecurityGroup;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.verses.VerseStatus;
import pl.psnc.vtl.portal.repositories.events.*;
import pl.psnc.vtl.portal.repositories.iiiftools.IIIFToolsRepository;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.repositories.users.UserRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.services.ValidationException;
import pl.psnc.vtl.portal.services.iiifserver.IIIFUriResolver;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionService;
import javax.persistence.EntityNotFoundException;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import static java.util.stream.Collectors.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private static final long EVENT_THUMBNAILS_FAKE_TRANSCRIPTION_ID = 0;

    private final EventRepository eventRepository;
    private final EventTeamRepository eventTeamRepository;
    private final EventAttendeeRepository eventAttendeeRepository;
    private final EventTranscriptionRepository eventTranscriptionRepository;
    private final EventTeamPageRepository eventTeamPageRepository;
    private final UserRepository userRepository;
    private final VerseRepository verseRepository;

    private final IIIFToolsRepository iiifToolsRepository;
    private final IIIFUriResolver iiifUriResolver;

    private final TranscriptionPermissionService transcriptionPermissionService;
    private final TranscriptionService transcriptionService;
    private final PageRepository pageRepository;

    private final EventMailService eventMailService;

    private final TransactionTemplate transactionTemplate;

    private final LoggedInUserHolder loggedInUserHolder;

    public void checkWriteAccess(long eventId) {
        checkWriteAccess(eventId, false);
    }

    public void checkWriteAccess(long eventId, boolean allowEnded) {
        Event event = eventRepository.getById(eventId);
        User user = loggedInUserHolder.getLoggedInUser();
        if (user == null) {
            throw new AccessDeniedException("User not logged in");
        }
        if (!isOrganizer(event, user)) {
            throw new EntityNotFoundException("Event not available: " + event.getId());
        }

        if (!allowEnded && event.isAnnounced() && event.getEndDate().isBefore(LocalDateTime.now()))
            throw new AccessDeniedException("Ended event can't be modified: " + event.getId());
    }

    private boolean isOrganizer(Event event, User user) {
        return user != null && (event.getOrganizers().stream().anyMatch(o -> o.getId().equals(user.getId()))
                || user.getSecurityGroup().equals(SecurityGroup.SUPER_ADMIN));
    }

    public void checkReadAccess(long eventId) {
        Event event = eventRepository.getById(eventId);
        if (event.isAnnounced())
            return;
        User user = loggedInUserHolder.getLoggedInUser();
        if (user == null)
            throw new AccessDeniedException("User not logged in");
        if (event.getAttendees().stream()
                .noneMatch(eventAttendee -> eventAttendee.getUser().getId().equals(user.getId()))) {
            checkWriteAccess(eventId);
        }
    }

    public Event createEvent(EventCore newEvent) {
        validate(newEvent);
        if (newEvent.isAnnounced())
            validateForAnnouncement(newEvent);
        Event event = new Event();
        update(event, newEvent);
        event = eventRepository.save(event);
        logUserAction("created event " + event.getId());
        return event;
    }

    public Event updateEvent(long eventId, EventCore eventCore) {
        validate(eventCore);
        Event event = eventRepository.getById(eventId);
        if (!event.isAnnounced() && eventCore.isAnnounced())
            validateForAnnouncement(eventCore);
        if (!event.isOpenForEnrollment() && eventCore.isOpenForEnrollment()) {
            validateForEventOpening(eventCore);
        }
        if (!eventCore.isAnnounced() && event.isAnnounced()) {
            BeanPropertyBindingResult errors = new BeanPropertyBindingResult(event, "Event");
            errors.rejectValue("announced", "unAnnouncing_AnnouncedEvent");
            throw new ValidationException(errors);
        }
        update(event, eventCore);
        logUserAction("updated event " + event.getId());
        return event;
    }

    private void validateForEventOpening(EventCore event) {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(event, "Event");

        if (event.isAnnounced() && event.getStartDate().isAfter(LocalDateTime.now())) {
            return;
        }
        errors.rejectValue("announced", "openingAlreadyStartedAndAnnouncedEvent");
        throw new ValidationException(errors);
    }

    public void setEventThumbnail(long eventId, InputStream fileStream, String fileName) {
        Event event = eventRepository.getById(eventId);
        if (event.getThumbnailName() != null) {
            iiifToolsRepository.deletePageImage(EVENT_THUMBNAILS_FAKE_TRANSCRIPTION_ID,
                    event.getThumbnailName());
            iiifToolsRepository.deletePagePyramid(EVENT_THUMBNAILS_FAKE_TRANSCRIPTION_ID,
                    event.getThumbnailPiramidName());
        }
        String fakePageName = String.format("%d_%05d.%s",
                eventId, RandomUtils.nextInt(0, 100000), fileName.replaceFirst(".+\\.", ""));
        iiifToolsRepository.savePageImage(EVENT_THUMBNAILS_FAKE_TRANSCRIPTION_ID, fileStream, fakePageName);
        String pyramidName =
                iiifToolsRepository.makePagePyramid(EVENT_THUMBNAILS_FAKE_TRANSCRIPTION_ID, fakePageName).get(0);
        String infoUrl = EVENT_THUMBNAILS_FAKE_TRANSCRIPTION_ID + "/" + pyramidName + "/info.json";
        Page fakePage = new Page();
        fakePage.setIiifUrl(infoUrl);
        event.setThumbnailName(fakePageName);
        event.setThumbnailPiramidName(pyramidName);
        event.setThumbnailUrl(iiifUriResolver.createPublicImageUrl(EVENT_THUMBNAILS_FAKE_TRANSCRIPTION_ID, pyramidName)
                .replace("/full/full/", "/full/^!430,290/"));
        eventRepository.save(event);
    }

    public void deleteEventThumbnail(long eventId) {
        Event event = eventRepository.getById(eventId);
        if (event.getThumbnailName() != null) {
            iiifToolsRepository.deletePageImage(EVENT_THUMBNAILS_FAKE_TRANSCRIPTION_ID,
                    event.getThumbnailName());
            iiifToolsRepository.deletePagePyramid(EVENT_THUMBNAILS_FAKE_TRANSCRIPTION_ID,
                    event.getThumbnailPiramidName());
            event.setThumbnailName(null);
            event.setThumbnailPiramidName(null);
            event.setThumbnailUrl(null);
            eventRepository.save(event);
        }
    }

    @Transactional(readOnly = true)
    public PaginatedEvents getEvents(EventCategory category, OnlyMine onlyMine, int page,
                                     int perpage) {

        List<Event> events;
        if (onlyMine != null) {
            events = findByOnlyMine(onlyMine);
        } else {
            events = eventRepository.findAll(Sort.by(Direction.DESC, "id"));
        }
        LocalDateTime now = LocalDateTime.now();
        if (category == EventCategory.UNANNOUNCED) {
            events.removeIf(Event::isAnnounced);
        } else if (category == EventCategory.ANNOUNCED) {
            events.removeIf(e -> {
                return !e.isAnnounced() || e.getStartDate().isBefore(now);
            });
            events.sort(Comparator.comparing(Event::getStartDate));
        } else if (category == EventCategory.ACTIVE) {
            events.removeIf(e -> !e.isAnnounced() || !e.getStartDate().isBefore(now) || e.getEndDate().isBefore(now));
            events.sort(Comparator.comparing(Event::getStartDate));
        } else if (category == EventCategory.COMPLETED) {
            events.removeIf(e -> !e.isAnnounced() || !e.getEndDate().isBefore(now));
            events.sort(Comparator.comparing(Event::getStartDate).reversed());
        }
        for (var it = events.iterator(); it.hasNext();) {
            try {
                checkReadAccess(it.next().getId());
            } catch (AccessDeniedException | EntityNotFoundException e) {
                it.remove();
            }
        }
        int hitsCount = events.size();
        int pageCount = hitsCount / perpage + (hitsCount % perpage > 0 ? 1 : 0);
        events = events.stream().skip((long) perpage * (page - 1)).limit(perpage).collect(toList());
        events.forEach(e -> {
            e.setOrganizers(null);
            User user = loggedInUserHolder.getLoggedInUser();
            e.getAttendees().removeIf(a -> user == null || !a.getUser().getId().equals(user.getId()));
        });
        return new PaginatedEvents(hitsCount, page, perpage, pageCount, events);
    }

    private List<Event> findByOnlyMine(OnlyMine onlyMine) {
        User currentUser = loggedInUserHolder.getLoggedInUser();
        if (currentUser == null) {
            throw new AccessDeniedException("User not logged in");
        }
        if (onlyMine == OnlyMine.ATTENDEND_BY_ME) {
            return eventRepository.findEventsByAttendee_Id(currentUser.getId());
        } else if (onlyMine == OnlyMine.ORGANIZED_BY_ME) {
            return eventRepository.findEventsByOrganizers_Id(currentUser.getId());
        } else if (onlyMine == OnlyMine.ORGANIZED_OR_ATTENDED_BY_ME) {
            return eventRepository.findEventsByOrganizer_IdOrAttendee_Id(currentUser.getId());
        }
        throw new IllegalArgumentException("Unknown OnlyMine value");
    }

    @Transactional(readOnly = true)
    public Event getEvent(long eventId) {
        Event event = eventRepository.getById(eventId);
        fillAttendeesStats(event);
        User user = loggedInUserHolder.getLoggedInUser();
        if (!isOrganizer(event, user)) {
            event.getAttendees().stream()
                    .filter(ea -> user == null || !ea.getUser().getId().equals(user.getId()))
                    .forEach(ea -> ea.getUser().setEmail(null));
        }
        return event;
    }

    private void fillAttendeesStats(Event event) {
        Map<Long, EventAttendee> attendeesByUserId =
                event.getAttendees().stream().collect(toMap(a -> a.getUser().getId(), a -> a));

        for (var verse : verseRepository.findByModificationLastEventId(event.getId())) {
            EventAttendee ea = attendeesByUserId.get(verse.getModificationLastEventAttendee().getId());
            if (ea == null || StringUtils.isBlank(verse.getContent())
                    || (verse.getStatus() != VerseStatus.TRANSCRIBED && verse.getStatus() != VerseStatus.VERIFIED)) {
                continue;
            }
            String[] words = verse.getContent().split("\\s+");
            ea.setVerseCount(ea.getVerseCount() + 1);
            ea.setWordCount(ea.getWordCount() + words.length);
            ea.setCharacterCount(ea.getCharacterCount() + Arrays.stream(words).mapToInt(String::length).sum());
        }
    }

    private void fillTeamsStats(Collection<EventTeamCore> teams, long eventId) {
        Event event = eventRepository.getById(eventId);
        if (!event.isAnnounced() || event.getStartDate().isAfter(LocalDateTime.now()))
            return;
        Map<Long, EventTeamCore> teamsByUserId = new HashMap<>();
        teams.forEach(t -> t.getMembers().forEach(m -> teamsByUserId.put(m.getUserId(), t)));

        for (var verse : verseRepository.findByModificationLastEventId(eventId)) {
            EventTeamCore team = teamsByUserId.get(verse.getModificationLastEventAttendee().getId());
            if (team == null || StringUtils.isBlank(verse.getContent())
                    || (verse.getStatus() != VerseStatus.TRANSCRIBED && verse.getStatus() != VerseStatus.VERIFIED)) {
                continue;
            }
            String[] words = verse.getContent().split("\\s+");
            team.setVerseCount(team.getVerseCount() + 1);
            team.setWordCount(team.getWordCount() + words.length);
            team.setCharacterCount(team.getCharacterCount() + Arrays.stream(words).mapToInt(String::length).sum());
        }
    }

    @Transactional(propagation = Propagation.NEVER)
    public void deleteEvent(long eventId) {

        Event[] event = { null };
        transactionTemplate.executeWithoutResult(status -> {
            event[0] = eventRepository.getById(eventId);

            List<Verse> verses = verseRepository.findByModificationLastEvent(event[0]);
            verses.forEach(verse -> verse.setModificationLastEvent(null));

            eventAttendeeRepository.deleteByEventId(eventId);
            eventTeamPageRepository.deleteByEventTeamEventId(eventId);
            eventTeamRepository.deleteByEventId(eventId);
            eventTranscriptionRepository.deleteByEventId(eventId);
            eventRepository.deleteById(eventId);
        });

        if (event[0].getThumbnailUrl() != null) {
            iiifToolsRepository.deletePageImage(EVENT_THUMBNAILS_FAKE_TRANSCRIPTION_ID,
                    event[0].getThumbnailName());
            iiifToolsRepository.deletePagePyramid(EVENT_THUMBNAILS_FAKE_TRANSCRIPTION_ID,
                    event[0].getThumbnailPiramidName());
        }
        logUserAction("deleted event " + eventId);
    }

    private void validate(EventCore event) {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(event, "Event");

        if (StringUtils.isBlank(event.getName()))
            errors.rejectValue("name", "nameRequired");

        List<String> organizerEmails = event.getOrganizerEmails();
        if (organizerEmails == null || organizerEmails.isEmpty()) {
            errors.rejectValue("organizerEmails", "organizerRequired");
        } else if (organizerEmails.stream().anyMatch(email -> !userRepository.existsByEmail(email))) {
            errors.rejectValue("organizerEmails", "organizerInvalid");
        }

        if (event.getStartDate() != null && event.getEndDate() != null
                && !event.getStartDate().isBefore(event.getEndDate())) {
            errors.rejectValue("startDate", "startEndInconsistent");
        }

        if (errors.hasErrors())
            throw new ValidationException(errors);
    }

    private void validateForAnnouncement(EventCore event) {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(event, "Event");

        if (event.getStartDate() == null || event.getEndDate() == null)
            errors.rejectValue("announced", "startEndMissingForAnnounced");

        if (event.getStartDate().isBefore(LocalDateTime.now()))
            errors.rejectValue("announced", "startInPastForAnnounced");

        // TODO validate teams?
        // TODO check all pages distributed?
        // TODO make sure all transcriptions available?

        if (errors.hasErrors())
            throw new ValidationException(errors);
    }

    private void update(Event event, EventCore core) {
        event.setName(core.getName());
        event.setOrganizers(core.getOrganizerEmails().stream().map(this::toUser).collect(toList()));
        event.setDescription(core.getDescription());
        event.setOpeningInfo(core.getOpeningInfo());
        event.setAnnounced(core.isAnnounced());
        event.setStartDate(core.getStartDate());
        event.setEndDate(core.getEndDate());
        event.setOpenForEnrollment(core.isOpenForEnrollment());
    }

    public void addAttendees(long eventId, List<String> attendeeEmails, Long teamId, String originUrl) {
        Event event = eventRepository.getById(eventId);
        if (event.isAnnounced() && event.getEndDate() != null && event.getEndDate().isBefore(LocalDateTime.now()))
            throw new AccessDeniedException("Event already ended");
        Set<String> existing = eventAttendeeRepository.findByEventId(eventId)
                .stream().map(a -> a.getUser().getEmail()).collect(toSet());
        User currentUser = loggedInUserHolder.getLoggedInUser();
        List<User> targetUsers = attendeeEmails.stream().distinct().filter(e -> !existing.contains(e)).map(this::toUser)
                .collect(toList());
        EventTeam team = teamId == null ? null : checkTeamInEvent(eventId, teamId);
        for (User user : targetUsers) {
            boolean confirmed = user.getId().equals(currentUser.getId());
            EventAttendee a = new EventAttendee(event, user, team, confirmed);
            eventAttendeeRepository.save(a);
            if (!confirmed)
                eventMailService.sendInvitationEmail(event, user, originUrl);
        }
        logUserAction("added attendees to event " + eventId + ": "
                + targetUsers.stream().map(u -> u.getId().toString()).collect(joining(", ")));
    }

    public void enrollCurrentUser(long eventId, String originUrl) {
        Event event = eventRepository.getById(eventId);
        if (!event.isOpenForEnrollment())
            throw new AccessDeniedException("Event is not open for enrollment");
        if (event.isAnnounced() && event.getEndDate() != null && event.getEndDate().isBefore(LocalDateTime.now()))
            throw new AccessDeniedException("Event already ended");

        addAttendees(eventId, List.of(loggedInUserHolder.getLoggedInUser().getEmail()), null, originUrl);
        confirmAttendee(eventId);
    }

    public void confirmAttendee(long eventId) {
        User user = loggedInUserHolder.getLoggedInUser();
        EventAttendee ea = eventAttendeeRepository.findByEventIdAndUser(eventId, user);
        if (ea == null) {
            throw new AccessDeniedException("User was not invited or already resigned.");
        } else {
            ea.setConfirmed(true);
        }
        eventAttendeeRepository.save(ea);
        logUserAction("confirmed attendance for event " + eventId);
    }

    public void removeAttendees(long eventId, List<String> emailsToRemove) {
        List<EventAttendee> attendees = eventAttendeeRepository.findByEventId(eventId).stream()
                .filter(a -> emailsToRemove.contains(a.getUser().getEmail()))
                .collect(toList());

        Event event = eventRepository.getById(eventId);
        if (event.isAnnounced() && event.getStartDate() != null && event.getStartDate().isBefore(LocalDateTime.now())
                && attendees.stream().anyMatch(EventAttendee::isConfirmed)) {
            throw new AccessDeniedException("Event already started and user already confirmed");
        }

        eventAttendeeRepository.deleteAll(attendees);
        logUserAction("removed attendees from event " + eventId + ": "
                + attendees.stream().map(ea -> ea.getUser().getId().toString()).collect(joining(", ")));
    }

    public List<EventTeamCore> setEventTeams(long eventId, List<EventTeamCore> newTeams) {
        validate(eventId, newTeams);
        Event event = eventRepository.getById(eventId);
        Map<Long, EventTeam> teamsById = eventTeamRepository.findByEventId(eventId)
                .stream().collect(toMap(t -> t.getId(), t -> t));
        Map<Long, EventAttendee> attendeesById = eventAttendeeRepository.findByEventId(eventId)
                .stream().collect(toMap(a -> a.getUser().getId(), a -> a));
        for (EventAttendee a : attendeesById.values())
            a.setTeam(null);

        for (EventTeamCore teamCore : newTeams) {
            EventTeam team = teamCore.getId() != null ? teamsById.remove(teamCore.getId()) : new EventTeam();
            team.setName(teamCore.getName());
            team.setEvent(event);
            eventTeamRepository.save(team);

            for (Member teamMember : teamCore.getMembers()) {
                EventAttendee a = attendeesById.computeIfAbsent(teamMember.getUserId(),
                        id -> new EventAttendee(event, userRepository.getById(id), team, false));
                a.setTeam(team);
            }
        }
        eventAttendeeRepository.saveAllAndFlush(attendeesById.values());
        eventAttendeeRepository.deleteByTeamIdIn(teamsById.keySet());
        eventTeamPageRepository.deleteByEventTeamIdIn(teamsById.keySet());
        eventTeamRepository.deleteAll(teamsById.values());
        logUserAction("updated teams in event " + eventId);
        return getEventTeams(eventId);
    }

    public List<EventTeamCore> getEventTeams(long eventId) {
        Map<EventTeam, EventTeamCore> results = eventTeamRepository.findByEventId(eventId).stream()
                .collect(toMap(t -> t, t -> new EventTeamCore(t.getId(), t.getName())));
        boolean includeEmails = isOrganizer(eventRepository.getById(eventId), loggedInUserHolder.getLoggedInUser());
        eventAttendeeRepository.findByEventId(eventId).stream()
                .filter(a -> a.getTeam() != null)
                .forEach(a -> results.get(a.getTeam()).getMembers().add(new Member(a.getUser(), includeEmails)));
        fillTeamsPages(results.values(), eventId);
        fillTeamsStats(results.values(), eventId);
        return results.values().stream()
                .sorted(Comparator.comparing(t -> Objects.requireNonNullElse(t.getId(), Long.MAX_VALUE)))
                .collect(toList());
    }

    /**
     * Sends message to all event attendees
     * @param title message title
     * @param content message content
     */
    public void sendMessagesToAttendees(long eventId, String title, String content, String origin){
        Event event = eventRepository.getById(eventId);
        eventMailService.sendEmailToAttendees(event, title, content, origin);
    }

    private void fillTeamsPages(Collection<EventTeamCore> teams, long eventId) {
        Map<Long, EventTeamCore> teamsById = teams.stream().collect(toMap(t -> t.getId(), t -> t));
        eventTeamPageRepository.getByEventTeamEventId(eventId)
                .forEach(etp -> teamsById.get(etp.getEventTeam().getId())
                        .getTranscriptionPages()
                        .computeIfAbsent(etp.getPage().getTranscription().getId(), k -> new ArrayList<>())
                        .add(etp.getPage().getId()));
    }

    private void validate(long eventId, List<EventTeamCore> eventTeams) {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(eventTeams, "EventTeam");
        HashSet<Long> idsDupl = new HashSet<>();
        for (EventTeamCore eventTeam : eventTeams) {
            if (StringUtils.isBlank(eventTeam.getName()))
                errors.rejectValue("name", "nameRequired");
            if (eventTeam.getMembers() != null) {
                if (!eventTeam.getMembers().stream().map(Member::getUserId).allMatch(userRepository::existsById))
                    errors.rejectValue("members", "invalidMemberId");
                if (!eventTeam.getMembers().stream().map(Member::getUserId).allMatch(idsDupl::add))
                    errors.rejectValue("membes", "duplicatedIds");
            }
            if (eventTeam.getId() != null) {
                EventTeam t = eventTeamRepository.getById(eventTeam.getId());
                if (eventId != t.getEvent().getId())
                    errors.rejectValue("id", "wrongEvent");
            }
        }
        if (errors.hasErrors())
            throw new ValidationException(errors);
    }

    private User toUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new RequestFieldsValidationException(new ErrorResponseMessage("email", "noSuchUser")));
    }

    public void addEventTranscriptions(long eventId, List<Long> transcriptionIds) {
        Event event = eventRepository.getById(eventId);
        List<EventTranscription> ets = new ArrayList<>();
        for (Long transcriptionId : transcriptionIds) {
            transcriptionPermissionService.checkTranscribeAccess(transcriptionId);
            Transcription t = transcriptionService.requireTranscription(transcriptionId);
            if (t.isHidden())
                throw new AccessDeniedException("transcription " + transcriptionId + " is hidden");
            ets.add(new EventTranscription(event, t));
        }
        eventTranscriptionRepository.deleteByEventIdAndTranscriptionIdIn(eventId, transcriptionIds);
        eventTranscriptionRepository.flush();
        eventTranscriptionRepository.saveAllAndFlush(ets);
        logUserAction("added transcriptions to event " + eventId + ": " + transcriptionIds);
    }

    public void removeEventTranscriptions(long eventId, List<Long> transcriptionIds) {
        eventTranscriptionRepository.deleteByEventIdAndTranscriptionIdIn(eventId, transcriptionIds);
        eventTranscriptionRepository.flush();
        eventTeamPageRepository.deleteByEventTeamEventIdAndPageTranscriptionIdIn(eventId, transcriptionIds);
        eventTeamPageRepository.flush();
        logUserAction("removed transcriptions from event " + eventId + ": " + transcriptionIds);
    }

    @Transactional(readOnly = true)
    public PaginatedTranscriptions<Transcription> getEventTranscriptions(long eventId, int page, int perpage) {
        var all = eventTranscriptionRepository.findTranscriptionsByEventId(eventId).stream()
                .filter(t -> transcriptionPermissionService.findViewMode(t.getId()) != null)
                .sorted(Comparator.comparing(Transcription::getTitleLabel))
                .collect(toList());
        var inPage = all.stream().skip((page - 1) * perpage).limit(perpage).collect(toList());
        inPage.forEach(t -> transcriptionService.completeTranscription(t, true, 1, 6));
        return new PaginatedTranscriptions<>(all.size(), inPage.size(), page, perpage,
                (all.size() + perpage - 1) / perpage, inPage);
    }

    public void addTeamPages(long eventId, long teamId, List<Long> pageIds) {
        EventTeam team = checkTeamInEvent(eventId, teamId);
        List<EventTeamPage> etps = new ArrayList<>();
        for (Long pageId : pageIds) {
            Page page = pageRepository.getById(pageId);
            if (!eventTranscriptionRepository.existsById(
                    new EventTranscriptionId(eventId, page.getTranscription().getId()))) {
                throw new RequestFieldsValidationException(
                        new ErrorResponseMessage("pageId", "transcriptionNotInEvent"));
            }
            etps.add(new EventTeamPage(team, page, null));
        }
        eventTeamPageRepository.deleteByEventTeamEventIdAndPageIdIn(eventId, pageIds);
        eventTeamPageRepository.flush();
        eventTeamPageRepository.saveAllAndFlush(etps);
        logUserAction("added pages for team " + teamId + " in event " + eventId + ": " + pageIds);
    }

    public void removeTeamPages(long eventId, long teamId, List<Long> pageIds) {
        checkTeamInEvent(eventId, teamId);
        eventTeamPageRepository.deleteByEventTeamIdAndPageIdIn(teamId, pageIds);
        eventTeamPageRepository.flush();
        logUserAction("removed pages from team " + teamId + " in event " + eventId + ": " + pageIds);
    }

    @Transactional(readOnly = true)
    public PaginatedPages getEventTranscriptionPages(long eventId, long transcriptionId, Long teamId,
            boolean includeUnassigned, int page, int perpage) {
        if (!eventTranscriptionRepository.existsById(
                new EventTranscriptionId(eventId, transcriptionId))) {
            throw new RequestFieldsValidationException(
                    new ErrorResponseMessage("transcriptionId", "transcriptionNotInEvent"));
        }
        List<Page> pages = pageRepository.findByTranscriptionIdOrderByPageNo(transcriptionId);
        List<EventTeamPage> etps =
                eventTeamPageRepository.getByEventTeamEventIdAndPageTranscriptionId(eventId, transcriptionId);
        Map<Long, EventTeam> page2team =
                etps.stream().collect(toMap(etp -> etp.getPage().getId(), etp -> etp.getEventTeam()));

        if (teamId != null) {
            pages.removeIf(p -> page2team.get(p.getId()) != null
                    && !teamId.equals(page2team.get(p.getId()).getId()));
        }
        if (!includeUnassigned) {
            pages.removeIf(p -> page2team.get(p.getId()) == null);
        }

        List<Page> results = pages.stream()
                .skip((long) (page - 1) * perpage)
                .limit(perpage)
                .map(p -> new EventPage(p, toEventTeamCore(page2team.get(p.getId()))))
                .collect(toList());

        return PaginatedPages.builder().list(results).count(results.size()).hits(pages.size())
                .page(page).perpage(perpage).pages((pages.size() + perpage - 1) / perpage).build();
    }

    private EventTeamCore toEventTeamCore(EventTeam eventTeam) {
        if (eventTeam == null)
            return null;
        return new EventTeamCore(eventTeam.getId(), eventTeam.getName());
    }

    private EventTeam checkTeamInEvent(long eventId, long teamId) {
        EventTeam et = eventTeamRepository.getById(teamId);
        if (et.getEvent().getId() != eventId)
            throw new RequestFieldsValidationException(new ErrorResponseMessage("teamId", "wrongEvent"));
        return et;
    }

    private void logUserAction(String action) {
        Long userId = loggedInUserHolder.getLoggedInUser().getId();
        LogAction.MODIFY_DATA.info(log, "User " + userId + " " + action);
    }

    public enum EventCategory {
        UNANNOUNCED, ANNOUNCED, ACTIVE, COMPLETED
    }

    public enum OnlyMine {
        ORGANIZED_BY_ME, ATTENDEND_BY_ME, ORGANIZED_OR_ATTENDED_BY_ME
    }

}

