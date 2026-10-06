package pl.psnc.vtl.portal.repositories.transcriptions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUserId;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.transcriptions.UserLastTranscription;
import pl.psnc.vtl.portal.model.users.User;

import java.util.List;


@Repository
public interface UserLastTranscriptionRepository extends JpaRepository<UserLastTranscription, TranscriptionUserId> {

    List<UserLastTranscription> findByUserOrderByLastModificationDateDesc(User user);
    List<UserLastTranscription> findByUser(User user);

    void deleteByTranscription(Transcription transcription);

}
