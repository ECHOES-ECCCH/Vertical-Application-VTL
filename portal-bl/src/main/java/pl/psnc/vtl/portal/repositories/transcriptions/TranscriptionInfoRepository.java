package pl.psnc.vtl.portal.repositories.transcriptions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.transcriptions.TranscriptionInfo;

import java.util.Collection;
import java.util.List;

@Repository
public interface TranscriptionInfoRepository extends JpaRepository<TranscriptionInfo, String> {

    List<TranscriptionInfo> findByIdIn(Collection<Long> ids);
}
