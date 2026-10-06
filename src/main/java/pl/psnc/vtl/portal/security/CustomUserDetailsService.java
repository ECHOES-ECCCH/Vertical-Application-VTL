package pl.psnc.vtl.portal.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.repositories.users.UserRepository;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;

@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(final String username) {
        return userRepository.findByEmail(username)
                .map(this::buildUserForAuthentication)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find user"));

    }

    private org.springframework.security.core.userdetails.User buildUserForAuthentication(User user) {
        return new CustomUser(user.getId(), user.getEmail(), user.getPassword(), user.isEnabled(), user.isTrustedContributor(), Collections.singleton(user.getSecurityGroup()));
    }
}
