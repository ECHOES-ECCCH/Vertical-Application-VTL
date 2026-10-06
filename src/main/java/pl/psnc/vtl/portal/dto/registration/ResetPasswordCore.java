package pl.psnc.vtl.portal.dto.registration;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reset password core object.
 */
@Data
@NoArgsConstructor
public class ResetPasswordCore {

    /**
     * Security token.
     */
    private String token;

    /**
     * New password.
     */
    private String newPassword;

    /**
     * Repeated new password.
     */
    private String repeatedNewPassword;

}
