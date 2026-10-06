package pl.psnc.vtl.portal.model.users;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User group object.
 */
@Entity
@IdClass(UserGroupId.class)
@Table(name = "user_groups")
@Data
@NoArgsConstructor
public class UserGroup implements Serializable {

    /**
     * User being a member of the group.
     */
    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

    /**
     * Group that the user is being member of.
     */
    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(name = "group_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Group group;

    /**
     * Users role in given group.
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH})
    @JoinColumn(name = "user_role_code", referencedColumnName = "code")
    private UserRole role;

    /**
     * Creation date of UserGroup object.
     */
    @CreationTimestamp
    private LocalDateTime creationDate;

}
