package pl.psnc.vtl.portal.security;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;

public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    public static final String USER_ATTRIBUTE_TOKEN = "token";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);
        HashMap<String, Object> attributes = new HashMap<>(user.getAttributes());
        OAuth2AccessToken token = userRequest.getAccessToken();
        attributes.put(USER_ATTRIBUTE_TOKEN, token);
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
                .getUserNameAttributeName();
        return new DefaultOAuth2User(user.getAuthorities(), attributes, userNameAttributeName);
    }
}
