package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.transcriptions.TranscriptionUserLastPage;
import pl.psnc.vtl.portal.model.transcriptions.UserLastTranscription;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.repositories.pages.PageLayersRepository;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionUserLastPageRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.UserLastTranscriptionRepository;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class ModificationTrackingService {

    private final LoggedInUserHolder loggedInUserHolder;

    private final PageLayersRepository pageLayersRepository;
    private final UserLastTranscriptionRepository userLastTranscriptionRepository;
    private final TranscriptionUserLastPageRepository transcriptionUserLastPageRepository;
    private final PageRepository pageRepository;

    public void trackModification(PageLayer.Id pageLayerId) {
        LocalDateTime now = LocalDateTime.now();
        User user = loggedInUserHolder.getLoggedInUser();
        PageLayer pageLayer = pageLayersRepository.getById(pageLayerId);
        pageLayer.setLastModificationDate(now);
        pageLayer.setLastModifiedBy(user.getEmail());

        Page page = pageLayer.getPage();
        trackModification(page, user, now);
        trackModification(page.getTranscription(), user, now);
    }

    public void trackModification(Transcription transcription) {
        LocalDateTime now = LocalDateTime.now();
        User user = loggedInUserHolder.getLoggedInUser();
        trackModification(transcription, user, now);
    }

    private void trackModification(Transcription transcription, User user, LocalDateTime now) {
        transcription.setLastModificationDate(now);

        UserLastTranscription userLastTranscription = new UserLastTranscription();
        userLastTranscription.setTranscription(transcription);
        userLastTranscription.setUser(user);
        userLastTranscription.setLastModificationDate(now);
        userLastTranscriptionRepository.save(userLastTranscription);
    }

    private void trackModification(Page page, User user, LocalDateTime now) {
        page.setLastModificationDate(now);
        page.setLastModifier(user.getEmail());

        TranscriptionUserLastPage transcriptionUserLastPage = new TranscriptionUserLastPage();
        transcriptionUserLastPage.setLastPage(page.getId());
        transcriptionUserLastPage.setTranscription(page.getTranscription());
        transcriptionUserLastPage.setUser(user);
        transcriptionUserLastPageRepository.save(transcriptionUserLastPage);
    }

    public void pageRemoved(Page page) {
        Page otherPage = pageRepository
                .findByTranscriptionIdAndPageNo(page.getTranscription().getId(), page.getPageNo() - 1);
        if (otherPage == null) {
            otherPage = pageRepository
                    .findByTranscriptionIdAndPageNo(page.getTranscription().getId(), page.getPageNo() + 1);
        }
        if (otherPage != null) {
            transcriptionUserLastPageRepository.setNewLastPageForTranscription(page.getTranscription(), page.getId(),
                    otherPage.getId());
        } else {
            transcriptionUserLastPageRepository.deleteByTranscriptionAndLastPage(page.getTranscription(), page.getId());
        }
    }
}
