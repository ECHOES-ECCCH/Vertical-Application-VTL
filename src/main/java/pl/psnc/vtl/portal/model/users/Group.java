package pl.psnc.vtl.portal.model.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.dto.users.UserGroupInline;

import javax.persistence.*;
import java.util.List;

/**
 * Group of users.
 */
@Entity
@Table(name = "groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "group_generator")
    @SequenceGenerator(name = "group_generator", sequenceName = "groups_id_seq", allocationSize = 50)
    private Long id;

    /**
     * Group name.
     */
    @Basic
    private String name;

    /**
     * Group creator.
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn
    private User creator;

    /**
     * Group members.
     */
    @Transient
    private List<UserGroupInline> members;

    /**
     * Is current user group owner.
     */
    @Transient
    private boolean owner;

}
