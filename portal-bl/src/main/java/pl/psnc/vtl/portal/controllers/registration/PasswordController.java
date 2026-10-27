package pl.psnc.vtl.portal.controllers.registration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.dto.registration.ResetPasswordCore;
import pl.psnc.vtl.portal.dto.registration.ResetPasswordRequest;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.services.registration.ResetPasswordService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Reset password", description = "Operations needed to reset user's password if the user  has forgotten it")
public class PasswordController {

    private final ResetPasswordService resetPasswordService;

    @Operation(summary = "Initiates reset password procedure. To be used when the user forgot his/hers password. If provided user email is correct, application will send an email to the user to confirm the reset operation")
    @PostMapping(path = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void sendEmailToResetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest, @RequestHeader("language") StaticFileLanguage lang) {
        resetPasswordRequest.setLang(lang);
        resetPasswordService.sendEmailToResetPassword(resetPasswordRequest);
    }


    @Operation(summary = "Changes the password based on a pair of 2 identical, new passwords. To be performed after verifying token validity." )
    @PostMapping(value = "/change-password")
    public void resetPassword(@RequestBody ResetPasswordCore resetPasswordCore) {
        resetPasswordService.resetPassword(resetPasswordCore);
    }

    @Operation(summary = "Validates user token used for reset password operation. Token should be validated if user visited the link he got from \"Reset Password\" email")
    @PutMapping(value = "/reset-password/token/{token}")
    public void validateToken(@PathVariable("token") String token) {
        resetPasswordService.validateToken(token);
    }

}
