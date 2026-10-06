package pl.psnc.vtl.portal.security;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider implements TokenProvider{

    private String jwtSecret;

    private Long jwtExpirationInMs;

    public JwtTokenProvider(
            @Value("${vtl.auth.jwt.secret-key}") String jwtSecret,
            @Value("${vtl.auth.jwt.max-age}") Long jwtExpirationInMs) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    @Override
    public String generateToken(Authentication authentication) {
        String userName = obtainUsername(authentication);
        // TODO handle expires flag

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return "Bearer " + Jwts.builder()
                .setSubject(userName)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    private String obtainUsername(Authentication authentication) {
        return ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();
    }

    @Override
    public String getUserIdFromToken(String token) {
        String jwtToken = token.substring(7, token.length());
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(jwtToken)
                .getBody();

        return claims.getSubject();
    }

    @Override
    public boolean validateToken(String token) {
        if (token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7, token.length());
            try {
                Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(jwtToken);
                return true;
            } catch (SignatureException ex) {
                log.error("Invalid JWT signature", ex);
            } catch (MalformedJwtException ex) {
                log.error("Invalid JWT token", ex);
            } catch (ExpiredJwtException ex) {
                log.error("Expired JWT token", ex);
            } catch (UnsupportedJwtException ex) {
                log.error("Unsupported JWT token", ex);
            } catch (IllegalArgumentException ex) {
                log.error("JWT claims string is empty.", ex);
            }
        }
        return false;
    }

}
