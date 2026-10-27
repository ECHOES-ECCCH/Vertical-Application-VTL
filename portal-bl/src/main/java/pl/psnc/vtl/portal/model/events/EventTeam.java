package pl.psnc.vtl.portal.model.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "event_teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventTeam {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_team_generator")
    @SequenceGenerator(name = "event_team_generator", sequenceName = "event_teams_id_seq", allocationSize = 50)
    @JsonIgnore
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH })
    @JoinColumn
    @JsonIgnore
    private Event event;

    @Basic
    @Column(nullable = false, length = 1024)
    private String name;
}
