package pl.psnc.vtl.portal.model.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * User object.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    public static final User UNKNOWN_USER = User
            .builder()
            .email("[deleted]")
            .securityGroup(SecurityGroup.USER)
            .quota(0L)
            .usedSpaceQuota(0L)
            .hasPassword(false)
            .enabled(false)
            .build();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Email address.
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Name
     */
    @Basic
    @Column(nullable = true)
    private String name;

    /**
     * Password
     */
    @Basic
    @Column(nullable = true)
    @JsonIgnore
    private String password;

    /**
     * Is user enabled.
     */
    @Basic
    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Does user have password.
     */
    @Basic
    @Column(columnDefinition = "BOOLEAN DEFAULT true", nullable = false)
    private boolean hasPassword = true;

    /**
     * Did user accept regulations.
     */
    @Basic
    @Column(nullable = false)
    @CreationTimestamp
    @JsonIgnore
    private LocalDateTime acceptedRegulations;

    /**
     * Users security group.
     */
    @Column(nullable = false, updatable = true)
    @Enumerated(EnumType.STRING)
    private SecurityGroup securityGroup = SecurityGroup.USER;

    /**
     * User space quota.
     */
    @Basic
    @Column(nullable = false)
    private Long quota;

    /**
     * User used space quota.
     */
    @Basic
    @Column(nullable = false)
    private Long usedSpaceQuota = 0L;

    /**
     * Is user a trusted contributor.
     */
    @Column(columnDefinition = "BOOLEAN DEFAULT false", nullable = false)
    private boolean trustedContributor = false;
}
