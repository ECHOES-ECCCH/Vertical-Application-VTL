package pl.psnc.vtl.portal.services.registration;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.support.ScopeNotActiveException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;
import pl.psnc.vtl.portal.model.users.SecurityGroup;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.repositories.users.UserRepository;
import pl.psnc.vtl.portal.security.CustomUser;
import pl.psnc.vtl.portal.services.imports.BeanLoader;

import static pl.psnc.vtl.portal.model.users.SecurityGroup.EXPORT_REVIEWER;
import static pl.psnc.vtl.portal.model.users.SecurityGroup.SUPER_ADMIN;

@Service
@RequiredArgsConstructor
public class LoggedInUserHolder {

    private static final ThreadLocal<User> threadLocal = new ThreadLocal<>();
    
    private final UserRepository userRepository;

    private final LoggedInUserCache cache;

    public static Runnable withLoggedInUser(Runnable runnable) {
        User user = getLoggedInUser(BeanLoader.getBean(UserRepository.class));
        return () -> {
            threadLocal.set(user);
            try {
                runnable.run();
            } finally {
                threadLocal.remove();
            }
        };
    }

    public User getLoggedInUser() {
        if (threadLocal.get() != null) {
            return threadLocal.get();
        }
        try {
            if (cache.getUser() != null) {
                return cache.getUser();
            }
            cache.setUser(getLoggedInUser(userRepository));
            return cache.getUser();
        } catch (ScopeNotActiveException e) {
            return null;
        }
    }

    private static User getLoggedInUser(UserRepository userRepository) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken || authentication == null) {
            return null;
        } else if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User user = oAuth2AuthenticationToken.getPrincipal();
            String email = user.getAttributes().get("email").toString();
            return userRepository.findByEmail(email).orElseThrow(
                    () -> new RuntimeException("User with email " + email + " not found"));
        } else {
            User user = new User();
            CustomUser customUser = (CustomUser) authentication.getPrincipal();
            user.setId(customUser.getId());
            user.setEmail(authentication.getName());
            user.setEnabled(customUser.isEnabled());
            user.setTrustedContributor(customUser.isTrustedContributor());
            user.setSecurityGroup(readUserRole(customUser));
            return user;
        }
    }

    public boolean isCurrentlyLoggedUserSuperadmin() {
        return SUPER_ADMIN.equals(getLoggedInUser().getSecurityGroup());
    }

    public boolean isCurrentlyLoggedUserExportReviewerOrAdmin() {
        return EXPORT_REVIEWER.equals(getLoggedInUser().getSecurityGroup()) || SUPER_ADMIN.equals(getLoggedInUser().getSecurityGroup());
    }

    private static SecurityGroup readUserRole(CustomUser customUser) {
        if (!customUser.getAuthorities().isEmpty()) {
            return (SecurityGroup) customUser.getAuthorities().iterator().next();
        } else {
            return null;
        }
    }

    @Component
    @RequestScope
    @Data
    private class LoggedInUserCache {
        private User user;
    }
}
