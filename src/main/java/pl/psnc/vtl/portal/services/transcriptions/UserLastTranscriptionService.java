package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.dto.transcriptions.SearchOrder;
import pl.psnc.vtl.portal.dto.transcriptions.SearchScope;
import pl.psnc.vtl.portal.dto.transcriptions.SearchVisibility;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.transcriptions.UserLastTranscription;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.repositories.transcriptions.UserLastTranscriptionRepository;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserLastTranscriptionService {

    private final UserLastTranscriptionRepository userLastTranscriptionRepository;

    private final TranscriptionService transcriptionService;

    private final TranscriptionListService transcriptionListService;

    private  final LoggedInUserHolder loggedInUserHolder;

    public Transcription getUserLastTranscription() {
        User loggedInUser = loggedInUserHolder.getLoggedInUser();
        List<UserLastTranscription> userLastTranscriptions = userLastTranscriptionRepository.findByUserOrderByLastModificationDateDesc(loggedInUser);
        if (!userLastTranscriptions.isEmpty()) {
            Transcription transcription = userLastTranscriptions.get(0).getTranscription();
            transcriptionService.completeTranscription(transcription, true, 1, 6);
            return transcription;
        }

        List<Transcription> transcriptions = transcriptionListService.getTranscriptions(
                null, null, SearchScope.CREATOR_OR_MEMBER, SearchVisibility.ALL, SearchOrder.LAST_MODIFICATION_DATE,
                null, 1, 1)
                .getTranscriptions();
        if (transcriptions.isEmpty()) {
            return null;
        }
        return transcriptions.get(0);
    }

    public List<Transcription> getUserLastTranscriptions() {
        return transcriptionListService.getTranscriptions(null, null, SearchScope.CREATOR_OR_MEMBER,
                SearchVisibility.ALL, SearchOrder.LAST_MODIFICATION_DATE, null, 1, 3).getTranscriptions();
    }

}
