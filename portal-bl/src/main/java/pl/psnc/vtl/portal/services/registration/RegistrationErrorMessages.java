package pl.psnc.vtl.portal.services.registration;

import lombok.Getter;

@Getter
public enum RegistrationErrorMessages {

    AUTH_ERROR_DIFFERENT_PASSWORDS("auth.error.differentPasswords"),
    AUTH_ERROR_INVALID_PASSWORD("auth.error.invalidPassword"),
    AUTH_ERROR_WRONG_PASSWORD("auth.error.wrongPassword"),
    AUTH_ERROR_NOT_ACCEPTED_REGULATIONS("auth.error.notAcceptedRegulations"),
    AUTH_ERROR_DUPLICATE_USERNAME("auth.error.duplicateUserName"),
    AUTH_ERROR_DUPLICATE_EMAIL("users.error.duplicateEmail"),
    AUTH_ERROR_INVALID_TOKEN("auth.error.invalidToken"),
    AUTH_ERROR_EXPIRED_TOKEN("auth.error.expiredToken"),
    AUTH_ERROR_NOT_EXISTING_EMAIL("auth.error.nonExistingEmail"),
    AUTH_ERROR_INVALID_EMAIL("users.error.invalidEmail"),
    AUTH_ERROR_INVALID_CAPTCHA("auth.error.invalidCaptcha");

    private final String message;

    RegistrationErrorMessages(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return this.message;
    }
}
