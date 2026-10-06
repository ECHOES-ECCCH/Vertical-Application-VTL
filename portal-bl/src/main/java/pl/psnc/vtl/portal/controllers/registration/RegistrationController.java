package pl.psnc.vtl.portal.controllers.registration;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.dto.registration.UserRegisterCore;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.services.registration.InvalidUserDataException;
import pl.psnc.vtl.portal.services.registration.RegistrationService;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Component responsible for registration process. Allows for:
 * <ul>
 *     <li>new user account creation</li>
 *     <li>newly created account activation</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Registration", description = "Operations needed for registering new user account")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    /**
     * Registers new user account based on provided parameters
     *
     * @param newUser description of the new user accout {@link UserRegisterCore}
     * @return newly created user account {@link User}
     * @throws InvalidUserDataException in case of wrongly specified parameters
     */

    @PostMapping(path = "/sign-up", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserRegisterCore newUser, @RequestHeader(value = "language", required = true)
            StaticFileLanguage lang)
            throws InvalidUserDataException {
        newUser.setLang(lang);
        return ResponseEntity.ok().body(registrationService.registerUser(newUser));
    }

    /**
     * Activates user account based on the provided token
     *
     * @param tokenValue token value that will be used for the activation
     */
    @PutMapping(path = "/activate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void activateAccount(@NotNull @RequestParam("token") String tokenValue) {
       registrationService.activateAccount(tokenValue);
    }

    @PutMapping(path = "/token")
    public void validateAuthorizationToken() {}

}
