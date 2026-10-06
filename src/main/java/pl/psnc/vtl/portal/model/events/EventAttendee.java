package pl.psnc.vtl.portal.model.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.users.User;

import javax.persistence.*;

@Entity
@IdClass(EventUserId.class)
@Table(name = "event_attendees")
@Data
@NoArgsConstructor
public class EventAttendee {
    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn
    @JsonIgnore
    private Event event;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn
    private User user;

    @ManyToOne(optional = true, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn
    private EventTeam team;

    @Basic
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean confirmed;

    @Transient
    private int characterCount;

    @Transient
    private int wordCount;

    @Transient
    private int verseCount;

    public EventAttendee(Event event, User user, EventTeam team, boolean confirmed) {
        this.event = event;
        this.user = user;
        this.team = team;
        this.confirmed = confirmed;
    }
}
