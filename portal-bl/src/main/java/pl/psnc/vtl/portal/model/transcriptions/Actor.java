package pl.psnc.vtl.portal.model.transcriptions;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Metadata Actor
 */
@Entity
@Table(name = "actors")
@Data
@NoArgsConstructor
public class Actor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actor_generator")
    @SequenceGenerator(name = "actor_generator", sequenceName = "actors_id_seq", allocationSize = 50)
    private Long id;

    /**
     * Name
     */
    @Basic
    @Column(nullable = false, length = 1024)
    private String name;

    /**
     * Role
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn
    private ActorRole role;

}
