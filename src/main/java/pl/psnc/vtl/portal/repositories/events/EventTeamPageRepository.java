package pl.psnc.vtl.portal.repositories.events;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.psnc.vtl.portal.model.events.EventTeamPage;
import pl.psnc.vtl.portal.model.events.EventTeamPageId;
import pl.psnc.vtl.portal.model.pages.Page;

import java.util.Collection;
import java.util.List;

public interface EventTeamPageRepository extends JpaRepository<EventTeamPage, EventTeamPageId> {

    @Query("select tp.page from EventTeamPage tp where tp.eventTeam.id = :eventTeamId "
            + "order by tp.page.transcription.titleLabel, tp.page.pageNo")
    List<Page> findPagesByEventTeamId(long eventTeamId);

    List<EventTeamPage> getByEventTeamEventId(long eventId);

    List<EventTeamPage> getByEventTeamEventIdAndPageTranscriptionId(long eventId, long transcriptionId);

    void deleteByEventTeamIdAndPageIdIn(long eventTeamId, List<Long> pageIds);

    void deleteByEventTeamEventIdAndPageTranscriptionIdIn(long eventId, List<Long> transcriptionIds);

    void deleteByEventTeamIdIn(Collection<Long> eventTeamIds);

    void deleteByEventTeamEventIdAndPageIdIn(long eventId, List<Long> pageIds);

    void deleteByEventTeamEventId(long eventId);

    EventTeamPage findByEventTeamIdAndPageId(Long id, long pageId);

}
