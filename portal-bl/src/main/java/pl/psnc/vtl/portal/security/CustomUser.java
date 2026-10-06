package pl.psnc.vtl.portal.security;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
@EqualsAndHashCode(callSuper = false)
public class CustomUser extends User {

    private final Long id;
    /**
     * trustedContributor
     */
    private boolean trustedContributor;



    public CustomUser(Long id, String username, String password, boolean enabled, boolean trustedContributor,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, true, true, true, authorities);
        this.id = id;
        this.trustedContributor = trustedContributor;
    }

}
