package pl.psnc.vtl.portal.services.users;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.model.events.Event;
import pl.psnc.vtl.portal.model.events.EventAttendee;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUser;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.transcriptions.TranscriptionUserLastPage;
import pl.psnc.vtl.portal.model.transcriptions.UserLastTranscription;
import pl.psnc.vtl.portal.model.users.Group;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.users.UserGroup;
import pl.psnc.vtl.portal.model.verses.Comment;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.verses.VerseContent;
import pl.psnc.vtl.portal.repositories.events.EventAttendeeRepository;
import pl.psnc.vtl.portal.repositories.events.EventRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionUserLastPageRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionUserRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.UserLastTranscriptionRepository;
import pl.psnc.vtl.portal.repositories.users.GroupRepository;
import pl.psnc.vtl.portal.repositories.users.UserGroupRepository;
import pl.psnc.vtl.portal.repositories.users.UserRepository;
import pl.psnc.vtl.portal.repositories.verses.CommentRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseContentRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionService;

import java.util.List;


/**
 * Service responsible for removing user (and all her/his related data) from system.
 */
@Service
@RequiredArgsConstructor
public class UserRemovalService {

    private final LoggedInUserHolder loggedInUserHolder;
    private final TranscriptionService transcriptionService;
    private final TranscriptionRepository transcriptionRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final GroupRepository groupRepository;
    private final EventRepository eventRepository;
    private final EventAttendeeRepository eventAttendeeRepository;
    private final TranscriptionUserRepository transcriptionUserRepository;
    private final TranscriptionUserLastPageRepository transcriptionUserLastPageRepository;
    private final UserLastTranscriptionRepository userLastTranscriptionRepository;
    private final CommentRepository commentRepository;
    private final VerseRepository verseRepository;
    private final VerseContentRepository verseContentRepository;

    public void removeLoggedInUser(boolean withPublicTranscriptions) {
        User loggedInUser = loggedInUserHolder.getLoggedInUser();
        removeUserTranscriptions(withPublicTranscriptions, loggedInUser);
        changeOwnershipOfUserGroups(loggedInUser);
        removeUserFromGroups(loggedInUser);
        changeOwnershipOfUserEvents(loggedInUser);
        removeUserFromEvents(loggedInUser);
        removeUserFromTranscriptions(loggedInUser);
        removeUserLastPagesInfo(loggedInUser);
        removeUserLastTranscriptions(loggedInUser);
        changeOwnershipOfUserComments(loggedInUser);
        changeOwnershipOfUserVerses(loggedInUser);
        removeUserFromVerseContents(loggedInUser);
        userRepository.delete(loggedInUser);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    private void removeUserTranscriptions(boolean withPublicTranscriptions, User loggedInUser) {
        List<Transcription> userTranscriptions = transcriptionRepository.findAllByCreator(loggedInUser);
        userTranscriptions.forEach(transcription -> {
            if (transcription.isHidden()) {
                transcriptionService.deleteTranscription(transcription.getId());
            } else {
                if (withPublicTranscriptions) {
                    transcriptionService.deleteTranscription(transcription.getId());
                } else {
                    transcription.setCreator(unknownUser());
                }
            }
        });
    }

    private void changeOwnershipOfUserGroups(User loggedInUser) {
        List<Group> userGroups = groupRepository.findByCreator(loggedInUser);
        userGroups.forEach(userGroup -> userGroup.setCreator(unknownUser()));
    }

    private void changeOwnershipOfUserEvents(User loggedInUser) {
        List<Event> eventsOrganizedByUser = eventRepository.findEventsByOrganizers_Id(loggedInUser.getId());
        eventsOrganizedByUser.forEach(event -> {
            List<User> organizers = event.getOrganizers();
            organizers.remove(loggedInUser);
            if (organizers.isEmpty()) {
                organizers.add(unknownUser());
            }
            event.setOrganizers(organizers);
        });
    }

    private void removeUserFromEvents(User loggedInUser) {
        List<EventAttendee> usersAttendedEvents = eventAttendeeRepository.findByUserId(loggedInUser.getId());
        eventAttendeeRepository.deleteAll(usersAttendedEvents);
    }

    private void removeUserFromGroups(User loggedInUser) {
        List<UserGroup> userGroups = userGroupRepository.findByUserId(loggedInUser.getId());
        userGroupRepository.deleteAll(userGroups);
    }

    private void removeUserFromTranscriptions(User loggedInUser) {
        List<TranscriptionUser> userTranscriptions = transcriptionUserRepository.findByUser(loggedInUser);
        transcriptionUserRepository.deleteAll(userTranscriptions);
    }

    private void removeUserLastPagesInfo(User loggedInUser) {
        List<TranscriptionUserLastPage> userLastPages = transcriptionUserLastPageRepository.findByUser(loggedInUser);
        transcriptionUserLastPageRepository.deleteAll(userLastPages);
    }

    private void removeUserLastTranscriptions(User loggedInUser) {
        List<UserLastTranscription> userLastTranscriptions = userLastTranscriptionRepository.findByUser(loggedInUser);
        userLastTranscriptionRepository.deleteAll(userLastTranscriptions);
    }

    private void changeOwnershipOfUserComments(User loggedInUser) {
        List<Comment> userComments = commentRepository.findByCreator(loggedInUser);
        userComments.forEach(comment -> comment.setCreator(unknownUser()));
    }

    private void changeOwnershipOfUserVerses(User loggedInUser) {
        List<Verse> verses = verseRepository.findByAuthor(loggedInUser);
        verses.forEach(verse -> verse.setAuthor(unknownUser()));

        verses = verseRepository.findByAuthorOfLastStatusModification(loggedInUser);
        verses.forEach(verse -> verse.setAuthor(unknownUser()));

        verses = verseRepository.findByModificationLastEventAttendee(loggedInUser);
        verses.forEach(verse -> verse.setAuthor(unknownUser()));
    }

    private void removeUserFromVerseContents(User loggedInUser) {
        List<VerseContent> userVerseContents = verseContentRepository.findByUser(loggedInUser);
        userVerseContents.forEach(verseContent -> verseContent.setUser(unknownUser()));

        userVerseContents = verseContentRepository.findByRejectedBy(loggedInUser);
        userVerseContents.forEach(verseContent -> verseContent.setUser(unknownUser()));
    }

    private User unknownUser() {
        return userRepository.findByEmail(User.UNKNOWN_USER.getEmail()).orElseGet(
                () -> userRepository.save(User.UNKNOWN_USER));
    }
}
