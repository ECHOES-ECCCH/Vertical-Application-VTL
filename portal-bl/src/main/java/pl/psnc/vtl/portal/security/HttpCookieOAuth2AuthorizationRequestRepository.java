package pl.psnc.vtl.portal.security;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String RETURN_URI_PARAM_COOKIE_NAME = "returnUrl";
    public static final String LANG_PARAM_COOKIE_NAME = "lang";
    private static final int cookieExpireSeconds = 18000;
    private final HttpSessionOAuth2AuthorizationRequestRepository httpSessionOAuth2AuthorizationRequestRepository = new HttpSessionOAuth2AuthorizationRequestRepository();

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return httpSessionOAuth2AuthorizationRequestRepository.loadAuthorizationRequest(request);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        httpSessionOAuth2AuthorizationRequestRepository.saveAuthorizationRequest(authorizationRequest, request, response);
        setCookie(request, response, RETURN_URI_PARAM_COOKIE_NAME);
        setCookie(request, response, LANG_PARAM_COOKIE_NAME);
    }

    private void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName) {
        String cookie = request.getParameter(cookieName);
        if (StringUtils.isNotBlank(cookie))
            CookieUtils.addCookie(response, cookieName, cookie, cookieExpireSeconds);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
        return httpSessionOAuth2AuthorizationRequestRepository.removeAuthorizationRequest(request);
    }

}