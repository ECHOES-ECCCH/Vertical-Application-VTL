package pl.psnc.vtl.portal.security;


import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import pl.psnc.vtl.portal.conf.OauthConfig;
import pl.psnc.vtl.portal.conf.OauthConfig.Client;
import pl.psnc.vtl.portal.conf.logging.LogAction;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.model.users.QuotaConfiguration;
import pl.psnc.vtl.portal.model.users.SecurityGroup;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.services.registration.RegistrationService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pl.psnc.vtl.portal.security.HttpCookieOAuth2AuthorizationRequestRepository.LANG_PARAM_COOKIE_NAME;
import static pl.psnc.vtl.portal.security.HttpCookieOAuth2AuthorizationRequestRepository.RETURN_URI_PARAM_COOKIE_NAME;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {


    private static final String ROLE_ACCESS = "access";
    private static final int COOKIE_MAX_AGE = 9000;
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirectUrl";

    private final RememberMeTokenProvider rememberMeTokenProvider;

    private final RegistrationService registrationService;
    private final QuotaConfiguration quotaConfiguration;
    private final OauthConfig oauthConfig;

    @Value("${vtl.oauth2.entitlements-role-pattern}")
    private String entitlementsRolePattern;

    private SecurityGroup getMaxRole(List<String> roles) {
        return roles.stream()
                .map(SecurityGroup::parse)
                .max(Comparator.naturalOrder())
                .orElse(SecurityGroup.PUBLIC);
    }


    private long getQuota(OAuth2User oAuthPrincipal) {
        //switch to default quota , ignore oauth details
        long quota;
        try {
            if (quotaConfiguration.getApplyFromClaims()) {
                quota = Integer.parseInt(oAuthPrincipal.getAttributes().get("BAD_quota").toString());
            } else {
                quota = quotaConfiguration.getSpace() * 1000000L;
            }
        } catch (Exception ex) {
            logger.error(
                    "Cannot get BAD_quota: " + ex.getMessage() + ", using default :" + quotaConfiguration.getSpace());
            quota = quotaConfiguration.getSpace() * 1000000L;
        }
        return quota;
    }

    private LocalDateTime getAcceptedTermsTimestamp(OAuth2User oAuthPrincipal) {
        LocalDateTime tsAcceptedTerms;
        try {
            Long ts = Long.parseLong(oAuthPrincipal.getAttributes().get("terms_and_conditions").toString());
            tsAcceptedTerms = LocalDateTime.ofEpochSecond(ts, 0, ZoneOffset.UTC);
        } catch (Exception ex) {
            tsAcceptedTerms = null;
        }
        return tsAcceptedTerms;
    }

    private String getName(OAuth2User oAuthPrincipal, String federationClient, HttpServletRequest request) {
        String name = Stream.of("nickname", "preferred_username", "name")
                .map(oAuthPrincipal::<String> getAttribute)
                .filter(Objects::nonNull).findFirst()
                .orElseThrow(() -> {
                    logFailure(federationClient, null, request.getRemoteAddr());
                    return new IllegalArgumentException("cannot sign-in user without defined name");
                });
        return name;
    }

    private String getEmail(OAuth2User oAuthPrincipal, String federationClient, String username,
                            HttpServletRequest request) {
        Object emailObject = oAuthPrincipal.getAttributes().get("email");
        String email = null;
        if (emailObject == null) {
            noEmailProvided(federationClient, username, request.getRemoteAddr());
        } else {
            email = emailObject.toString();
        }
        Object emailConfirmed = oAuthPrincipal.getAttributes().get("email_verified");
        if (emailConfirmed == null || !Boolean.parseBoolean(emailConfirmed.toString())) {
            noEmailNotConfirmed(federationClient, email, request.getRemoteAddr());
        }
        return email;
    }

    private String getLang(HttpServletRequest request) {
        String lang;
        Optional<Cookie> langOptional = CookieUtils.getCookie(request, LANG_PARAM_COOKIE_NAME);
        if (langOptional.isEmpty()) {
            //TODO: Set default lang
            lang = "";
        } else lang = langOptional.get().getValue();
        return lang;
    }

    @SneakyThrows
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String federationClient = extractClient(request);

        OAuth2User oAuthPrincipal = ((OAuth2AuthenticationToken) authentication).getPrincipal();
        List<String> userRoles = getUserRoles((OAuth2AuthenticationToken) authentication);
        SecurityGroup userRole = getMaxRole(userRoles);
        String name = getName(oAuthPrincipal, federationClient, request);
        String email = getEmail(oAuthPrincipal, name, federationClient, request);
        long quota = getQuota(oAuthPrincipal);
        LocalDateTime tsAcceptedTerms = getAcceptedTermsTimestamp(oAuthPrincipal);
        String lang = getLang(request);
        // create user
        User user = registrationService.syncOrCreateUser(email, name, federationClient,
                StaticFileLanguage.ofOrDefault(lang), userRole, quota, tsAcceptedTerms,
                userRoles.contains(ROLE_ACCESS));
        updateAuthorities(authentication, user, oAuthPrincipal);

        LogAction.setMDCAction(LogAction.LOGIN);
        log.info(String.format("SUCCES - %s login for %s, login date: %s, client IP: %s", federationClient,
                email, LocalDateTime.now(), request.getRemoteAddr()));
        LogAction.clearMDCAction();

        // redirect succesful login
        String token = rememberMeTokenProvider.generateToken(user.getEmail());
        CookieUtils.addSecureCookie(response, "remember-me", token, COOKIE_MAX_AGE);

        Optional<Cookie> cookie = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME);
        if (cookie.isEmpty())
            cookie = CookieUtils.getCookie(request, RETURN_URI_PARAM_COOKIE_NAME);
        String redirectUrl = cookie.isEmpty() ? "/" : cookie.get().getValue();
        removeAuthorizationRequestCookies(request, response);
        response.sendRedirect(redirectUrl);
    }


    public void updateAuthorities(Authentication auth, User user, OAuth2User oAuthPrincipal) throws Exception {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Set<GrantedAuthority> authorities = new HashSet<>(auth.getAuthorities());
        // add whatever authorities you want here
        authorities.add(new SimpleGrantedAuthority(user.getSecurityGroup().getAuthority()));
        Authentication newAuth = new OAuth2AuthenticationToken(oAuthPrincipal, authorities,
                (((OAuth2AuthenticationToken) auth).getAuthorizedClientRegistrationId()));

        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    private void logFailure(String client, String username, String ip) {
        LogAction.setMDCAction(LogAction.LOGIN);
        log.info(String.format("FAILURE - %s login for %s, login date: %s, client IP: %s", client, username,
                LocalDateTime.now(), ip));
        LogAction.clearMDCAction();
    }

    static String extractClient(HttpServletRequest request) {
        return request.getServletPath().replaceAll(".*/([^/]+)/?", "$1");
    }

    private void noEmailProvided(String client, String username, String ip) {
        logFailure(client, username, ip);
        throw new IllegalArgumentException("cannot sign-in with oauth without having email");
    }

    private void noEmailNotConfirmed(String client, String username, String ip) {
        logFailure(client, username, ip);
        throw new IllegalArgumentException("cannot sign-in with not confirmed email");
    }


    private void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
        CookieUtils.deleteCookie(request, response, RETURN_URI_PARAM_COOKIE_NAME);
    }

    private List<String> getUserRoles(OAuth2AuthenticationToken authToken) throws ParseException {
        if (StringUtils.isNotBlank(entitlementsRolePattern)) {
            List<Object> entitlements = authToken.getPrincipal().getAttribute("entitlements");
            Pattern pattern = Pattern.compile(entitlementsRolePattern);
            List<String> roles = new ArrayList<>();
            for (Object entitlement : entitlements) {
                Matcher matcher = pattern.matcher(entitlement.toString());
                if (matcher.matches())
                    roles.add(matcher.group(1));
            }
            roles.add(ROLE_ACCESS); // TODO hardcoded for now for simplicity
            return roles;
        } else {
            Client client = oauthConfig.getRegistration().get(authToken.getAuthorizedClientRegistrationId());
            Object token = authToken.getPrincipal().getAttribute(CustomOAuth2UserService.USER_ATTRIBUTE_TOKEN);
            JWT jwt = JWTParser.parse(((OAuth2AccessToken) token).getTokenValue());
            JSONObject resourceAccess = (JSONObject) (jwt.getJWTClaimsSet().getClaim("resource_access"));
            if (resourceAccess == null)
                return List.of();

            JSONObject wlt = (JSONObject) resourceAccess.get(client.getClientId());
            JSONArray roles = (JSONArray) wlt.get("roles");
            return roles.stream().map(Object::toString).collect(Collectors.toList());
        }
    }

}