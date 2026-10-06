package pl.psnc.vtl.portal.repositories.events;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.psnc.vtl.portal.model.events.EventTranscription;
import pl.psnc.vtl.portal.model.events.EventTranscriptionId;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;

import java.util.List;

public interface EventTranscriptionRepository extends JpaRepository<EventTranscription, EventTranscriptionId> {

    @Query("SELECT et.transcription FROM EventTranscription et where et.event.id = :eventId")
    List<Transcription> findTranscriptionsByEventId(long eventId);

    List<EventTranscription> findByTranscriptionId(long transcriptionId);

    void deleteByEventId(Long eventId);

    void deleteByEventIdAndTranscriptionIdIn(long eventId, List<Long> transcriptionIds);
}
