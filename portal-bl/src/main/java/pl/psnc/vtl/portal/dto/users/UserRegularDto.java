package pl.psnc.vtl.portal.dto.users;

import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.users.SecurityGroup;
import pl.psnc.vtl.portal.model.users.User;

import java.time.LocalDateTime;

/**
 * User regular dto object
 */
@NoArgsConstructor
@Getter
public class UserRegularDto {

    /**
     * User id.
     */
    private Long id;

    /**
     * User email.
     */
    private String email;

    /**
     * User name.
     */
    private String name;

    /**
     * Is user enabled.
     */
    private boolean enabled;

    /**
     * Does user have password.
     */
    private boolean hasPassword;

    /**
     * Did user accept regulations.
     */
    private LocalDateTime acceptedRegulations;

    /**
     * Users security group.
     */
    private SecurityGroup securityGroup;

    /**
     * Users max allowed quota.
     */
    private Integer quota;

    /**
     * Users used up quota.
     */
    private Integer usedSpaceQuota;

    /**
     * Is user a trusted contributor.
     */
    private boolean trustedContributor;

    public UserRegularDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.enabled = user.isEnabled();
        this.hasPassword = user.isHasPassword();
        this.acceptedRegulations = user.getAcceptedRegulations();
        this.securityGroup = user.getSecurityGroup();
        this.quota = user.getQuota() != null ? (int) (user.getQuota() / 1000000) : 0;
        this.usedSpaceQuota = user.getUsedSpaceQuota() != null ? (int) (user.getUsedSpaceQuota() / 1000000) : 0;
        this.trustedContributor = user.isTrustedContributor();
    }
}
