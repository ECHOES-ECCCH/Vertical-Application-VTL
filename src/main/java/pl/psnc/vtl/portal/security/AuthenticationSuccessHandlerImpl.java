package pl.psnc.vtl.portal.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import pl.psnc.vtl.portal.conf.logging.LogAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Slf4j
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    private static final int COOKIE_MAX_AGE = 1000000;
    private final RememberMeTokenProvider rememberMeTokenProvider;
    private final TokenProvider jwtTokenProvider;
    private final String REMEMBER_ME;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        response.addHeader("Authorization", jwtTokenProvider.generateToken(authentication));

        String userUsername = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal())
                .getUsername();

        LogAction.setMDCAction(LogAction.LOGIN);
        log.info(String.format("SUCCESS - Normal login for %s, login date: %s, client IP: %s", userUsername,
                LocalDateTime.now(), request.getRemoteAddr()));
        LogAction.clearMDCAction();

        if (request.getParameter(REMEMBER_ME) == null)
            return;
        if (Boolean.valueOf(request.getParameter(REMEMBER_ME)).equals(true)) {
            String token = rememberMeTokenProvider.generateToken(authentication);
            CookieUtils.addCookie(response, REMEMBER_ME, token, COOKIE_MAX_AGE);
        }

    }
}
