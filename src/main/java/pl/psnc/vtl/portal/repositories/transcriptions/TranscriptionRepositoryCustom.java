package pl.psnc.vtl.portal.repositories.transcriptions;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import pl.psnc.vtl.portal.dto.transcriptions.*;
import pl.psnc.vtl.portal.model.users.User;

public interface TranscriptionRepositoryCustom {

    Page<TranscriptionWithHitCount> searchTranscriptions(String q, SearchType type, User user, SearchScope scope,
            SearchVisibility visibility, SearchOrder order, Sort.Direction orderDirToImpose, int page, int perpage);
}
