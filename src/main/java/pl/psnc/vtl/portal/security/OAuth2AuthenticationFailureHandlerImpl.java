package pl.psnc.vtl.portal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import pl.psnc.vtl.portal.conf.logging.LogAction;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static pl.psnc.vtl.portal.security.HttpCookieOAuth2AuthorizationRequestRepository.RETURN_URI_PARAM_COOKIE_NAME;

@Slf4j
public class OAuth2AuthenticationFailureHandlerImpl implements AuthenticationFailureHandler {

    private static ObjectMapper objectMapper;

    private static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
        }
        return objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            AuthenticationException e)
            throws IOException {

        if ("[authorization_request_not_found] ".equals(e.getMessage())) {
            String redirect = WebSecurityConfig.OAUTH_AUTHORIZATION_ENDPOINT + "/"
                    + OAuth2AuthenticationSuccessHandler.extractClient(httpServletRequest)
                    + CookieUtils.getCookie(httpServletRequest, RETURN_URI_PARAM_COOKIE_NAME)
                            .map(c -> "?returnUrl=" + URLEncoder.encode(c.getValue(), UTF_8)).orElse("");
            httpServletResponse.sendRedirect(redirect);
            return;
        }

        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());

        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", LocalDateTime.now());
        data.put("exception", e.getMessage());

        try (ServletOutputStream out = httpServletResponse.getOutputStream()) {
            out.println(getObjectMapper().writeValueAsString(data));
        }
        String federationClient = extractClient(httpServletRequest);

        LogAction.setMDCAction(LogAction.LOGIN);
        log.info(String.format("FAILURE - login with %s, date: %s, client IP: %s", federationClient,
                LocalDateTime.now(), httpServletRequest.getRemoteAddr()));
        log.debug("Authentication error full stack trace:", e);
        LogAction.clearMDCAction();
    }

    private String extractClient(HttpServletRequest request) {
        return request.getServletPath().replaceAll(".*/([^/]+)/?", "$1");
    }
}
