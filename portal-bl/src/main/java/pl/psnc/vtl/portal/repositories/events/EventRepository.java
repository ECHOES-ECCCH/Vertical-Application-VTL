package pl.psnc.vtl.portal.repositories.events;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.events.Event;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT DISTINCT e FROM Event e JOIN e.organizers o LEFT JOIN EventAttendee ea ON e.id = ea.event.id " +
            "WHERE (o.id =:userId) OR ea.user.id=:userId")
    List<Event> findEventsByOrganizer_IdOrAttendee_Id(long userId);

    @Query("SELECT DISTINCT e FROM Event e JOIN e.organizers o LEFT JOIN EventAttendee ea ON e.id = ea.event.id " +
            "WHERE (o.id =:userId) AND ea.user.id=:userId")
    List<Event> findEventsByOrganizer_IdAndAttendee_Id(long userId);

    List<Event> findEventsByOrganizers_Id(long userId);

    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN EventAttendee ea ON e.id = ea.event.id " +
            "WHERE ea.user.id=:userId")
    List<Event> findEventsByAttendee_Id(long userId);

}
