package pl.psnc.vtl.portal.services.registration;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.dto.registration.ResetPasswordCore;
import pl.psnc.vtl.portal.dto.registration.ResetPasswordRequest;
import pl.psnc.vtl.portal.model.registration.PersistenceToken;
import pl.psnc.vtl.portal.model.registration.TokenType;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.repositories.users.UserRepository;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ResetPasswordService {

    private final PersistenceTokenProvider persistanceTokenProvider;
    private final ResetPasswordEmailService resetPasswordEmailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public void sendEmailToResetPassword(ResetPasswordRequest resetPasswordRequest){
        // PasswordValidationException (not emailNotFound) due to security issues
        User user = userRepository.findByEmail(resetPasswordRequest.getEmail()).orElseThrow(
                () -> new PasswordValidationException("invalid password", "passwordChangeFailure"));
        PersistenceToken passwordResetToken = persistanceTokenProvider.createToken(user, TokenType.RESET_PASSWORD_TOKEN);
        EmailData emailData = new EmailData(user, passwordResetToken, resetPasswordRequest.getClientRedirectUrl(), resetPasswordRequest.getLang());
        resetPasswordEmailService.sendEmail(emailData);
    }

    //TODO validate token - DB request , get
    public void resetPassword(ResetPasswordCore resetPasswordCore) {
        if(!isValidPassword(resetPasswordCore))
            throw new PasswordValidationException("invalid password", "invalid password");
        validateToken(resetPasswordCore.getToken());
        User user = persistanceTokenProvider.getUserbyToken(resetPasswordCore.getToken());
        user.setPassword(passwordEncoder.encode(resetPasswordCore.getNewPassword()));
        user.setHasPassword(true);
        userRepository.save(user);
        persistanceTokenProvider.deleteTokenByUserAndTokenType(user, TokenType.RESET_PASSWORD_TOKEN);
    }

    public void validateToken(String token) {
        persistanceTokenProvider.validateToken(token, TokenType.RESET_PASSWORD_TOKEN);
    }

    protected boolean isValidPassword(ResetPasswordCore resetPasswordCore){
        if (resetPasswordCore.getNewPassword().equals(resetPasswordCore.getRepeatedNewPassword()))
            if (PasswordValidator.isValidPassword(resetPasswordCore.getNewPassword()))
                return true;
        return false;
    }
}
