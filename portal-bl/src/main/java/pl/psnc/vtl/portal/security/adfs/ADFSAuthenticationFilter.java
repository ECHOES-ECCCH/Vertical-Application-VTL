package pl.psnc.vtl.portal.security.adfs;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.psnc.vtl.portal.conf.logging.LogAction;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.security.CustomUser;
import pl.psnc.vtl.portal.security.DecryptionService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class ADFSAuthenticationFilter extends OncePerRequestFilter {

    private static final JsonPath JSON_PATH_UID = JsonPath.compile("$.uid");

    private static final JsonPath JSON_PATH_EMAIL = JsonPath.compile("$.email");

    private final DecryptionService decryptionService;

    private final ADFSService adfsService;

    private final AuthenticationSuccessHandler authenticationSuccessHandler;

    private Optional<String> obtainUserInfo(HttpServletRequest httpServletRequest) {
        return Optional.ofNullable(httpServletRequest.getParameter("email"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        attemptAuthentication(httpServletRequest, httpServletResponse);
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    public void attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException, IOException, ServletException {
        Optional<String> userInfo = obtainUserInfo(httpServletRequest);
        userInfo.ifPresent(info -> {
            // We have to replace all the space characters with + sign (which are normally valid elements of Base64 encoded string)
            // because somehow + signs come in the request as spaces (it's like in the browser when URL might contain spaces but they are replaced with + signs)
            String decrypted = decryptionService.decryptValue(info.replace(" ", "+"));

            try {
                ADFSIdentifier identifier = new ADFSIdentifier(JSON_PATH_UID.read(decrypted), JSON_PATH_EMAIL.read(decrypted));
                Authentication authentication = authenticate(identifier, StaticFileLanguage.ofOrDefault(httpServletRequest.getHeader("Language")));
                authenticationSuccessHandler.onAuthenticationSuccess(httpServletRequest, httpServletResponse, authentication);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (IOException | ServletException | IllegalArgumentException | PathNotFoundException e) {
                LogAction.setMDCAction(LogAction.LOGIN);
                log.info(String.format("FAILURE - ADFS login for %s, login date: %s, client IP: %s", userInfo,
                        LocalDateTime.now(), httpServletRequest.getRemoteAddr()));
                LogAction.clearMDCAction();
                throw new AuthenticationServiceException("Email could not be verified");
            }
        });
    }

    private Authentication authenticate(ADFSIdentifier identifier, StaticFileLanguage language) {
        User user = adfsService.getOrCreateUser(identifier, language);
        CustomUser customUser = createCustomUser(user);
        return new ADFSAuthenticationToken(
                customUser, Collections.singleton(user.getSecurityGroup()));
    }

    private CustomUser createCustomUser(User user) {
        return new CustomUser(user.getId(), user.getEmail(), user.getPassword(),
                user.isEnabled(), user.isTrustedContributor(),
                Collections.singleton(user.getSecurityGroup()));
    }

}
