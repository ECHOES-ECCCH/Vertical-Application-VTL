package pl.psnc.vtl.portal.repositories.events;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.psnc.vtl.portal.model.events.EventAttendee;
import pl.psnc.vtl.portal.model.events.EventUserId;
import pl.psnc.vtl.portal.model.users.User;

import java.util.Collection;
import java.util.List;

public interface EventAttendeeRepository extends JpaRepository<EventAttendee, EventUserId> {

    List<EventAttendee> findByEventId(long eventId);

    List<EventAttendee> findByUserId(long userId);

    EventAttendee findByEventIdAndUser(long eventId, User user);

    void deleteByEventId(Long eventId);

    void deleteByTeamIdIn(Collection<Long> teamIds);
}
