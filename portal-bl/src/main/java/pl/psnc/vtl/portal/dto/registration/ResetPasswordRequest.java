package pl.psnc.vtl.portal.dto.registration;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;

/**
 * Reset password request object.
 */
@Data
@NoArgsConstructor
public class ResetPasswordRequest {

    /**
     * Static file language.
     */
    private StaticFileLanguage lang;

    /**
     * Redirect url.
     */
    private String clientRedirectUrl;

    /**
     * User email.
     */
    private String email;

}
