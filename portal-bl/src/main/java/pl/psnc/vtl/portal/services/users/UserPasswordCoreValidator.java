package pl.psnc.vtl.portal.services.users;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.controllers.ErrorResponseMessage;
import pl.psnc.vtl.portal.controllers.RequestFieldsValidationException;
import pl.psnc.vtl.portal.dto.users.UserPasswordCore;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.repositories.users.UserRepository;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;
import pl.psnc.vtl.portal.services.registration.PasswordValidator;
import pl.psnc.vtl.portal.services.registration.RegistrationErrorMessages;

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class UserPasswordCoreValidator {

    private final UserRepository userRepository;
    private final LoggedInUserHolder loggedInUserHolder;

    public void validate(UserPasswordCore userPasswordCore){

        User user = loggedInUserHolder.getLoggedInUser();
        user = userRepository.findByEmail(user.getEmail()).orElseThrow(
                () -> new EntityNotFoundException("Unable to find user"));

        ErrorResponseMessage messeges = new ErrorResponseMessage();

        if (!PasswordValidator.isValidPasswordHash(userPasswordCore.getCurrentPassword(), user.getPassword()))
            messeges.add("currentPassword", RegistrationErrorMessages.AUTH_ERROR_INVALID_PASSWORD.getMessage());
        if (!PasswordValidator.isValidPassword(userPasswordCore.getNewPassword()))
            messeges.add("newPassword", RegistrationErrorMessages.AUTH_ERROR_WRONG_PASSWORD.getMessage());
        if (!PasswordValidator.isValidPasswordMatches(userPasswordCore.getNewPassword(), userPasswordCore.getRepeatedNewPassword()))
            messeges.add("repeatedNewPassword", RegistrationErrorMessages.AUTH_ERROR_DIFFERENT_PASSWORDS.getMessage());

        if(!messeges.isEmpty()) throw new RequestFieldsValidationException(messeges);

    }

}
