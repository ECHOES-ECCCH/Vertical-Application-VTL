package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.dto.permissions.ViewMode;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionContinuation;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUserId;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.transcriptions.TranscriptionUserLastPage;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionUserLastPageRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TranscriptionContinuationService {

    private final TranscriptionUserLastPageRepository transcriptionUserLastPageRepository;
    private  final LoggedInUserHolder loggedInUserHolder;
    private final PageRepository pageRepository;

    private final VerseRepository verseRepository;


    public TranscriptionContinuation constructContinuation(Transcription transcription) {
        if (transcription.getViewMode() == ViewMode.READ_ONLY) {
            return null;
        }
        Long lastPageId = getTranscriptionLastPageId(transcription);
        if (lastPageId != null) {
            if (verseRepository.countByPageTranscriptionId(transcription.getId()) == 0) {
                return new TranscriptionContinuation(lastPageId, TranscriptionContinuation.State.NOT_STARTED);
            } else {
                if (checkFinished(transcription)) {
                    return new TranscriptionContinuation(null, TranscriptionContinuation.State.FINISHED);
                } else {
                    return new TranscriptionContinuation(lastPageId, TranscriptionContinuation.State.IN_PROGRESS);
                }
            }
        } else {
            if (pageRepository.countByTranscriptionId(transcription.getId()) == 0) {
                return new TranscriptionContinuation(null, TranscriptionContinuation.State.NOT_STARTED);
            } else {
                if (verseRepository.countByPageTranscriptionId(transcription.getId()) == 0) {
                    return new TranscriptionContinuation(findFirstPageId(transcription), TranscriptionContinuation.State.NOT_STARTED);
                } else {
                    if (checkFinished(transcription)) {
                        return new TranscriptionContinuation(null, TranscriptionContinuation.State.FINISHED);
                    } else {
                        return new TranscriptionContinuation(findFirstPageId(transcription), TranscriptionContinuation.State.NOT_STARTED);
                    }
                }
            }
        }
    }


    private Long getTranscriptionLastPageId(Transcription transcription) {
        User loggedInUser = loggedInUserHolder.getLoggedInUser();
        TranscriptionUserId transcriptionUserId = new TranscriptionUserId(transcription.getId(), loggedInUser.getId());
        TranscriptionUserLastPage transcriptionUserLastPage = transcriptionUserLastPageRepository
                .findById(transcriptionUserId).orElse(null);
        if (transcriptionUserLastPage != null) {
            return transcriptionUserLastPage.getLastPage();
        } else {
            return null;
        }
    }

    private boolean checkFinished(Transcription transcription) {
        if (transcription.getViewMode() == ViewMode.TRANSCRIBE) {
            return (verseRepository.countByTranscriptionAndUntranscribedStatuses(transcription.getId()) == 0);
        } else {
            return (verseRepository.countByTranscriptionAndUnverifiedStatuses(transcription.getId()) == 0);
        }
    }

    private Long findFirstPageId(Transcription transcription) {
        return pageRepository.findByTranscriptionIdAndPageNo(transcription.getId(), 1).getId();
    }

}
