package pl.psnc.vtl.portal.repositories.transcriptions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUserId;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.transcriptions.TranscriptionUserLastPage;
import pl.psnc.vtl.portal.model.users.User;

import java.util.List;

@Repository
public interface TranscriptionUserLastPageRepository extends JpaRepository<TranscriptionUserLastPage, TranscriptionUserId> {

    @Modifying
    @Query("UPDATE TranscriptionUserLastPage SET lastPage = :newLastPage WHERE transcription = :transcription AND lastPage = :lastPage")
    void setNewLastPageForTranscription(@Param("transcription") Transcription transcription, @Param("lastPage") Long lastPage, Long newLastPage);

    void deleteByTranscriptionAndLastPage(Transcription transcription, Long lastPage);

    void deleteByTranscriptionId(Long transcriptionId);

    List<TranscriptionUserLastPage> findByUser(User user);
}

