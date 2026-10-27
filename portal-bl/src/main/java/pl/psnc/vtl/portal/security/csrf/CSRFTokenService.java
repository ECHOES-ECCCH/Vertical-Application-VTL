package pl.psnc.vtl.portal.security.csrf;

import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.web.util.matcher.RequestMatcher;
import pl.psnc.vtl.portal.security.CookieUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.regex.Pattern;

public final class CSRFTokenService {

    private final String CSRF_KEY;
    private static final String CSRF_TOKEN = "CSRF-TOKEN";
    private static final String X_CSRF_TOKEN = "X-CSRF-TOKEN";

    private static final String REMEMBER_ME = "remember-me";
    private final RequestMatcher csrfMatcher = new DefaultRequiresCsrfMatcher();

    private final MessageDigest digest;


    public CSRFTokenService(String csrfKey) {
        CSRF_KEY = csrfKey;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No MD5 algorithm available!");
        }
    }

    public boolean isValidToken(String rememberMeToken, HttpServletRequest request) {
        if (!csrfMatcher.matches(request)) return true;
        String csrfToken = generateCSRFToken(rememberMeToken);
        return Optional.ofNullable(request.getHeader(X_CSRF_TOKEN))
                .map(csrfToken::equals)
                .orElse(false);
    }

    public void generateCSRFToken(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.getCookie(request, REMEMBER_ME)
                .map(Cookie::getValue)
                .ifPresent(rememberMeToken -> response.setHeader(CSRF_TOKEN, generateCSRFToken(rememberMeToken)));
    }


    private String generateCSRFToken(String rememberMeToken) {
        String data = rememberMeToken.concat(CSRF_KEY);
        return new String(Hex.encode(digest.digest(data.getBytes())));
    }

    public static final class DefaultRequiresCsrfMatcher implements RequestMatcher {
        private final Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");
        private final Pattern allowedURIs = Pattern.compile("(/api/auth/.+)$");

        @Override
        public boolean matches(HttpServletRequest request) {
            return !allowedMethods.matcher(request.getMethod()).matches() &&
                    !allowedURIs.matcher(request.getRequestURI()).matches();
        }
    }

}


