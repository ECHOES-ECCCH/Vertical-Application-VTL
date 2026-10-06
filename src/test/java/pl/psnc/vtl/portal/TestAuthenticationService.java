package pl.psnc.vtl.portal;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.psnc.vtl.portal.model.users.SecurityGroup;
import pl.psnc.vtl.portal.security.CustomUser;
import pl.psnc.vtl.portal.security.adfs.ADFSAuthenticationToken;

import java.util.Collections;

public class TestAuthenticationService {

    public static void authenticateUser(long id, String email) {
        CustomUser user = new CustomUser(id, email, "password", true, false, Collections.singleton(SecurityGroup.USER));
        Authentication authentication = new ADFSAuthenticationToken(
                user, Collections.singleton(SecurityGroup.USER));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
