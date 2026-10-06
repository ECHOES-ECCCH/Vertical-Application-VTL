package pl.psnc.vtl.portal.dto.users;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User password core object.
 */
@Data
@NoArgsConstructor
public class UserPasswordCore {

    /**
     * Current password.
     */
    private String currentPassword;

    /**
     * New password.
     */
    private String newPassword;

    /**
     * Repeated user password.
     */
    private String repeatedNewPassword;
}
