package pl.psnc.vtl.portal.security;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import pl.psnc.vtl.portal.security.csrf.CSRFTokenService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class RememberMeServiceImpl implements RememberMeServices {

    private final String REMEMBER_ME;
    private final CustomUserDetailsService customUserDetailsService;
    private final RememberMeTokenProvider tokenProvider;
    private final JwtTokenProvider jwtTokenProvider;
    private final CSRFTokenService csrfTokenService;
    private final String KEY;


    /**
     * returns null in case of failed authentication, client {@link RememberMeAuthenticationFilter} expects authentication to be null when authentication failed
     */
    @Override
    public Authentication autoLogin(HttpServletRequest request, HttpServletResponse response) {
        Optional<Authentication> authentication = CookieUtils.getCookie(request, REMEMBER_ME)
                .map(Cookie::getValue)
                .filter(t -> csrfTokenService.isValidToken(t, request))
                .map(this::authenticate);
        authentication.ifPresent(a -> response.addHeader("Authorization", jwtTokenProvider.generateToken(a)));
        return authentication.orElse(null);
    }

    private Authentication authenticate(String token) {
        return Stream.of(token)
                .filter(tokenProvider::validateToken)
                .map(tokenProvider::getUserIdFromToken)
                .map(customUserDetailsService::loadUserByUsername)
                .map(this::generateAuthentication)
                .findFirst()
                .orElse(null);
    }

    private Authentication generateAuthentication(UserDetails userDetails) {
        Authentication authentication = new RememberMeAuthenticationToken(KEY, userDetails,
                userDetails.getAuthorities());
        return authentication;
    }


    @Override
    public void loginFail(HttpServletRequest request, HttpServletResponse response) {
    }


    @Override
    public void loginSuccess(HttpServletRequest request, HttpServletResponse response,
                             Authentication successfulAuthentication) {
    }
}
