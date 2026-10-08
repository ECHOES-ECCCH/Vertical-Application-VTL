package pl.psnc.vtl.portal.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;

@Component
public class EchoesAccessTokenProvider {

    public String getAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            throw new AccessDeniedException("ECHOES import requires OAuth2 authentication");
        }

        OAuth2AuthenticationToken oauth = (OAuth2AuthenticationToken) authentication;
        OAuth2AccessToken token = oauth.getPrincipal().getAttribute(CustomOAuth2UserService.USER_ATTRIBUTE_TOKEN);
        if (token == null || token.getTokenValue() == null || token.getTokenValue().isBlank()) {
            throw new AccessDeniedException("ECHOES access token is not available");
        }
        return token.getTokenValue();
    }
}