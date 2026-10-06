package pl.psnc.vtl.portal.security;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;

import javax.servlet.http.HttpServletRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Configuration
@Data
@Validated
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "bsadbad")
public class SecurityConfiguration {

    /**
     * send email after positive registration
     */
    @Value("${registration-email: false}")
    Boolean registrationEmail;

    @Value("sso-logout")
    String ssoLogout;

    @Value("post-logout-redirect-uri")
    String postLogoutRedirectUri;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public String getPostLogoutRedirectUri(HttpServletRequest request, Authentication authentication) {
        String redirectUri = request.getParameter("post_logout_redirect_uri");
        if (StringUtils.isBlank(redirectUri))
            redirectUri = postLogoutRedirectUri;

        String idToken = null;
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User user = ((OAuth2AuthenticationToken) authentication).getPrincipal();
            if (user instanceof OidcUser)
                idToken = ((OidcUser) user).getIdToken().getTokenValue();
        }
        if (isNotBlank(ssoLogout) && isNotBlank(redirectUri) && isNotBlank(idToken)) {
            return ssoLogout + "?post_logout_redirect_uri="
                    + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                    + "&id_token_hint=" + idToken;
        } else if (isNotBlank(redirectUri)) {
            return redirectUri;
        } else {
            return null;
        }
    }

}
