package pl.psnc.vtl.portal.conf.auth;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import pl.psnc.vtl.portal.security.RememberMeTokenProvider;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

@Slf4j
public class RememberMeTokenProviderTest {

    private final RememberMeTokenProvider provider = new RememberMeTokenProvider("key", 666L);
    private Authentication authentication;

    @Before
    public void init(){
        authentication = new RememberMeAuthenticationToken("key", "email@", Collections.emptyList());
    }


    @Test
    public void validateToken() {
        assertEquals(true, provider.validateToken(provider.generateToken(authentication)));

    }


    @Test
    public void getUserIdFromToken() {
        assertEquals("email@", provider.getUserIdFromToken(provider.generateToken(authentication)));
    }
}