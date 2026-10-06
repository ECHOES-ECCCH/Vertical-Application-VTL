package pl.psnc.vtl.portal.model.users;

import org.springframework.security.core.GrantedAuthority;

/**
 * User security group.
 */
public enum SecurityGroup implements GrantedAuthority {

    PUBLIC(0),
    ADFS_USER(1),
    USER(2),
    EXPORT_REVIEWER(3),
    SUPER_ADMIN(4);

    public final int priority;

    private SecurityGroup(int priority) {
        this.priority = priority;
    }

    @Override
    public String getAuthority() {
        return this.name();
    }


    public static int compareTo(SecurityGroup current, SecurityGroup other) {
        return Integer.compare(current.priority, other.priority);
    }

    public static SecurityGroup parse(String role) {
        if (role == null)
            return SecurityGroup.PUBLIC;
        switch (role.toLowerCase()) {
            case "public":
                return SecurityGroup.PUBLIC;
            case "role_adfs_user":
                return SecurityGroup.ADFS_USER;
            case "scientist":
            case "role_user":
                return SecurityGroup.USER;
            case "export-reviewer":
                return SecurityGroup.EXPORT_REVIEWER;
            case "administrator":
            case "role_super_admin":
                return SecurityGroup.SUPER_ADMIN;
            default:
                return SecurityGroup.PUBLIC;
        }
    }
}