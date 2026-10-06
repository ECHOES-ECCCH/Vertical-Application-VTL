package pl.psnc.vtl.portal.services.registration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.controllers.ErrorResponseMessage;
import pl.psnc.vtl.portal.controllers.RequestFieldsValidationException;
import pl.psnc.vtl.portal.dto.registration.UserRegisterCore;
import pl.psnc.vtl.portal.repositories.users.UserRepository;
import pl.psnc.vtl.portal.services.users.EmailValidator;

import static pl.psnc.vtl.portal.services.registration.RegistrationErrorMessages.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegistrationRequestValidator {

    private final UserRepository userRepository;

    private final CaptchaValidator captchaValidator;

    public void validate(UserRegisterCore userRegistrationRequest){

        ErrorResponseMessage messeges = new ErrorResponseMessage();

        if (!userRegistrationRequest.isAcceptedRegulations()) {
            messeges.add("acceptedRegulations", AUTH_ERROR_NOT_ACCEPTED_REGULATIONS.getMessage());
        }

        if (!EmailValidator.isValidEmail(userRegistrationRequest.getEmail()))
            messeges.add("email" , AUTH_ERROR_INVALID_EMAIL.getMessage());


        if (userRepository.existsByEmail(userRegistrationRequest.getEmail())) {
            messeges.add("email", AUTH_ERROR_DUPLICATE_EMAIL.getMessage());
        }

        if (!PasswordValidator.isValidPassword(userRegistrationRequest.getPassword()))
            messeges.add("password", AUTH_ERROR_INVALID_PASSWORD.getMessage());

        if (!PasswordValidator.isValidPasswordMatches(userRegistrationRequest.getPassword(), userRegistrationRequest.getRepeatedPassword()))
            messeges.add("repeatedPassword", AUTH_ERROR_DIFFERENT_PASSWORDS.getMessage());

        if (!captchaValidator.isValidCaptcha(userRegistrationRequest.getCaptcha()))
            messeges.add("captcha", AUTH_ERROR_INVALID_CAPTCHA.getMessage());

            log.info(messeges.toString());
        if (!messeges.isEmpty()) {
            throw new RequestFieldsValidationException(messeges);
        }
    }
}
