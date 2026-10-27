package pl.psnc.vtl.portal.services.registration;

import lombok.Getter;
import pl.psnc.vtl.portal.dto.registration.UserRegistrationErrorResponse;

@Getter
public class InvalidUserDataException extends RuntimeException {

    private UserRegistrationErrorResponse detailedMessage;

    public InvalidUserDataException(String message, UserRegistrationErrorResponse detailedMessage) {
        super(message);
        this.detailedMessage = detailedMessage;
    }
}
