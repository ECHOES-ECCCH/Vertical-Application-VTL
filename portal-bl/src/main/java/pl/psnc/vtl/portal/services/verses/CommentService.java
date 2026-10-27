package pl.psnc.vtl.portal.services.verses;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.dto.permissions.ViewMode;
import pl.psnc.vtl.portal.dto.verses.CommentCore;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.verses.Comment;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.verses.VerseStatus;
import pl.psnc.vtl.portal.repositories.verses.CommentRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.services.events.EventParticipationService;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;
import pl.psnc.vtl.portal.services.transcriptions.ModificationTrackingService;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {
    private  final LoggedInUserHolder loggedInUserHolder;

    private final VerseService verseService;

    private final CommentRepository commentRepository;

    private final EventParticipationService eventParticipationService;
    private final TranscriptionPermissionService transcriptionPermissionService;
    private final ModificationTrackingService modificationTrackingService;

    private final VerseRepository verseRepository;

    public List<Comment> getComments(Long verseId) {
        Verse verse = verseRepository.findById(verseId).orElseThrow(
                () -> new EntityNotFoundException(
                        "Unable to find " + Verse.class + " with id '" + verseId + "'!")
        );
        if (!eventParticipationService.isPageAccessible(verse.getPage().getId()))
            transcriptionPermissionService.checkTranscribeAccess(verse.getPage().getTranscription().getId());
        return commentRepository.findByVerseIdOrderByDateCreatedDesc(verseId);

    }


    public Integer addComment(Long verseId, CommentCore commentCore) {
        Verse verse = verseService.getVerseToMakeAction(verseId);

        Comment comment = new Comment();
        comment.setVerse(verse);
        comment.setContent(commentCore.getContent());
        comment.setDateCreated(LocalDateTime.now());
        comment.setCreator(loggedInUserHolder.getLoggedInUser());
        commentRepository.save(comment);
        return commentRepository.countByVerse(verse);
    }


    private Comment getCommentToMakeAction(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Comment.class + " with id '" + commentId + "'!"));
        User user = loggedInUserHolder.getLoggedInUser();
        if (!comment.getCreator().getId().equals(user.getId())) {
            throw new AccessDeniedException("Comment created by other user");
        }
        ViewMode viewMode = eventParticipationService.isPageAccessible(comment.getVerse().getPage().getId())
                ? ViewMode.TRANSCRIBE
                : transcriptionPermissionService.findViewMode(comment.getVerse().getPage().getTranscription().getId());
        if (viewMode == ViewMode.READ_ONLY) {
            throw new AccessDeniedException("This transcription is read only");
        }
        if (comment.getVerse().getStatus().equals(VerseStatus.VERIFIED) && viewMode == ViewMode.TRANSCRIBE) {
            throw new AccessDeniedException("Verse already verified");
        }
        modificationTrackingService.trackModification(comment.getVerse().getPageLayer().getId());
        return comment;
    }

    public void updateComment(Long commentId, CommentCore commentCore) {
        Comment existingComment = getCommentToMakeAction(commentId);
        existingComment.setContent(commentCore.getContent());
        commentRepository.save(existingComment);
    }


    public void deleteComment(Long commentId) {
        getCommentToMakeAction(commentId);
        commentRepository.deleteById(commentId);
    }
}
