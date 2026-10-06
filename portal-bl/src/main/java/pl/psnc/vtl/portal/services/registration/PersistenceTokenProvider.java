package pl.psnc.vtl.portal.services.registration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.model.registration.PersistenceToken;
import pl.psnc.vtl.portal.model.registration.TokenType;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.repositories.token.PersistenceTokenRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersistenceTokenProvider {

    private final PersistenceTokenRepository persistenceTokenRepository;

    @Value("${vtl.auth.password-reset-token-max-age}")
    protected Long TOKEN_MAX_AGE;

    protected PersistenceToken createToken(User user, TokenType tokenType) {
        PersistenceToken passwordResetToken = new PersistenceToken();
        passwordResetToken.setUser(user);
        passwordResetToken.setToken(generateToken());
        passwordResetToken.setTokenType(tokenType);
        passwordResetToken.setExpiryDate(LocalDateTime.now().plusSeconds(TOKEN_MAX_AGE));
        persistenceTokenRepository.save(passwordResetToken);
        return passwordResetToken;
    }

    private String generateToken(){
        return UUID.randomUUID().toString();
    }

    protected User getUserbyToken(String tokenValue){
        PersistenceToken token = persistenceTokenRepository.findById(tokenValue).orElseThrow(
                ()->  new InvalidTokenException("Token does not exist", RegistrationErrorMessages.AUTH_ERROR_INVALID_TOKEN.getMessage())
        );
        return token.getUser();
    }

    public PersistenceToken getTokenByValue(String tokenValue) {
        return persistenceTokenRepository.findById(tokenValue).orElseThrow(
                () -> new InvalidTokenException("Token does not exist", RegistrationErrorMessages.AUTH_ERROR_INVALID_TOKEN.getMessage()));
    }

    protected boolean isRightType(PersistenceToken persistenceToken, TokenType tokenType){
        return tokenType.equals(persistenceToken.getTokenType());
    }

    protected boolean isExpired(PersistenceToken persistanceToken){
        return LocalDateTime.now().compareTo(persistanceToken.getExpiryDate()) > 0;
    }

    public void deleteTokenByValue(String token) {
        persistenceTokenRepository.deleteById(token);
    }

    public void deleteTokenByUserAndTokenType(User user, TokenType type) {
        persistenceTokenRepository.deleteAllByUserAndTokenType(user, type);
    }

    public void validateToken(String token, TokenType tokenType) {
        PersistenceToken persistenceToken = getTokenByValue(token);
        if (!isExpired(persistenceToken))
            if(isRightType(persistenceToken, tokenType))
                return;
        throw new InvalidTokenException("Invalid Token", "Invalid Token");
    }
}
