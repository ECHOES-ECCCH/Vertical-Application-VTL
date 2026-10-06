package pl.psnc.vtl.portal.dto.registration;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * User register core object.
 */
@Data
@NoArgsConstructor
public class UserRegisterCore {

    /**
     * Password.
     */
    @NotNull
    @NotEmpty
    private String password;

    /**
     * Repeated password.
     */
    @NotNull
    @NotEmpty
    private String repeatedPassword;

    /**
     * Email.
     */
    @NotNull
    @NotEmpty
    private String email;

    /**
     * Does user accept regulations.
     */
    @NotNull
    private boolean acceptedRegulations;

    /**
     * Captcha.
     */
    @NotNull
    @NotEmpty
    private String captcha;

    /**
     * Redirect url.
     */
    @NotNull
    @NotEmpty
    private String clientRedirectURL;

    /**
     * Language.
     */
    private StaticFileLanguage lang;

}
