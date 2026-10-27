package pl.psnc.vtl.portal.model.transcriptions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Metadata actor role
 */
@Entity
@Table(name = "actor_roles")
@Getter
@Setter
@NoArgsConstructor
public class ActorRole {

    /**
     * Code
     */
    @Id
    private String code;

    /**
     * Ord
     */
    @Basic
    @Column(nullable = false)
    private Integer ord;

    /**
     * Pl
     */
    @Column
    private String pl;

    /**
     * En
     */
    @Column
    private String en;


}
