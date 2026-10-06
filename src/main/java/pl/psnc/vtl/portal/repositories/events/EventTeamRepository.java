package pl.psnc.vtl.portal.repositories.events;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.psnc.vtl.portal.model.events.EventTeam;

import java.util.List;

public interface EventTeamRepository extends JpaRepository<EventTeam, Long> {

    List<EventTeam> findByEventId(long eventId);

    void deleteByEventId(Long eventId);
}
