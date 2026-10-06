package pl.psnc.vtl.portal.model.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import pl.psnc.vtl.portal.model.pages.Page;

import javax.persistence.*;

import java.time.LocalDateTime;

@Entity
@IdClass(EventTeamPageId.class)
@Table(name = "event_team_pages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventTeamPage {
    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn
    private EventTeam eventTeam;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn
    private Page page;

    @CreationTimestamp
    private LocalDateTime timeAdded;
}
