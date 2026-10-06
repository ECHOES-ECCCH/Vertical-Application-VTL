package pl.psnc.vtl.portal.model.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pl.psnc.vtl.portal.conf.datetime.ApiDateTimeFormatter;
import pl.psnc.vtl.portal.model.users.User;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_generator")
    @SequenceGenerator(name = "event_generator", sequenceName = "events_id_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, length = 1024)
    private String name;

    @Column(length = 4095)
    private String description;

    @Column(length = 2047)
    private String openingInfo;

    @ManyToMany(cascade = { CascadeType.REFRESH })
    private List<User> organizers;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean announced;

    private boolean openForEnrollment;

    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.dateTimePattern)
    private LocalDateTime creationDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.dateTimePattern)
    private LocalDateTime startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.dateTimePattern)
    private LocalDateTime endDate;

    @Column(length = 1023)
    private String thumbnailUrl;

    @Column(length = 63)
    @JsonIgnore
    private String thumbnailName;

    @Column(length = 63)
    @JsonIgnore
    private String thumbnailPiramidName;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<EventAttendee> attendees;

}
