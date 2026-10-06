package pl.psnc.vtl.portal.services.registration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import pl.psnc.vtl.portal.model.registration.PersistenceToken;
import pl.psnc.vtl.portal.model.registration.TokenType;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.repositories.token.PersistenceTokenRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PersistenceTokenProviderTest {

    @Mock
    PersistenceTokenRepository tokenRepository;

    @InjectMocks
    PersistenceTokenProvider tokenProvider;

    @Test
    public void getUserbyToken() {
        String tokenValue = "token";
        User user = User.builder()
                .email("email")
                .build();
        PersistenceToken token = new PersistenceToken();
        token.setUser(user);
        doReturn(Optional.of(token)).when(tokenRepository).findById(tokenValue);
        assertEquals(user, tokenProvider.getUserbyToken(tokenValue));
        verify(tokenRepository, times(1)).findById(tokenValue);
    }


    @Test
    public void deleteTokenByValue() {
        String tokenValue = "token";
        doNothing().when(tokenRepository).deleteById(tokenValue);
        tokenProvider.deleteTokenByValue(tokenValue);
        verify(tokenRepository, times(1)).deleteById(tokenValue);
    }


    @Test(expected = InvalidTokenException.class)
    public void validateTokenThrowsExceptionWhenWronTypeGiven() {
        String tokenValue = "token";
        ReflectionTestUtils.setField(tokenProvider, "TOKEN_MAX_AGE", 666L);
        PersistenceToken token = new PersistenceToken();
        token.setExpiryDate(LocalDateTime.now().plusNanos(666L));
        token.setTokenType(TokenType.REGISTRATION_TOKEN);
        doReturn(Optional.of(token)).when(tokenRepository).findById(tokenValue);
        tokenProvider.validateToken(tokenValue, TokenType.RESET_PASSWORD_TOKEN);
    }

    @Test(expected = InvalidTokenException.class)
    public void validateTokenThrowsExceptionWhenExpiredToken() {
        String tokenValue = "token";
        ReflectionTestUtils.setField(tokenProvider, "TOKEN_MAX_AGE", 1L);
        PersistenceToken token = new PersistenceToken();
        token.setExpiryDate(LocalDateTime.now());
        token.setTokenType(TokenType.REGISTRATION_TOKEN);
        doReturn(Optional.of(token)).when(tokenRepository).findById(tokenValue);
        tokenProvider.validateToken(tokenValue, TokenType.RESET_PASSWORD_TOKEN);
    }
}