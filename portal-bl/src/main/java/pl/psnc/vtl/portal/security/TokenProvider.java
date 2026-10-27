package pl.psnc.vtl.portal.security;

import org.springframework.security.core.Authentication;

public interface TokenProvider {

    public String generateToken(Authentication authentication);

    public boolean validateToken(String token);

    public String  getUserIdFromToken(String token);
}
