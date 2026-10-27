package pl.psnc.vtl.portal.repositories.transcriptions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUser;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUserId;
import pl.psnc.vtl.portal.model.users.User;

import java.util.List;

@Repository
public interface TranscriptionUserRepository extends JpaRepository<TranscriptionUser, TranscriptionUserId> {

    List<TranscriptionUser> findByTranscriptionId(Long transcriptionId);

    List<TranscriptionUser> findByTranscriptionIdIn(List<Long> transcriptionId);

    List<TranscriptionUser> findByUser(User user);

    List<TranscriptionUser> deleteByTranscriptionId(Long transcriptionId);

}
