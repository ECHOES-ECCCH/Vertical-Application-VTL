package pl.psnc.vtl.portal.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

@Component
public class RememberMeTokenProvider implements TokenProvider {

    private final String key;

    private final Long tokenExpireTime;

    public RememberMeTokenProvider(
            @Value("${vtl.auth.remember-me.secret-key}") String key,
            @Value("${vtl.auth.remember-me.max-age}") Long tokenExpireTime) {
        this.key = key;
        this.tokenExpireTime = tokenExpireTime;
    }

    /**
     base64(userId + ":" + expirationTime + ":" + md5Hex(userId + ":" + expirationTime + ":" + key))
    */
    @Override
    public String generateToken(Authentication authentication) {
        String email = authentication.getName();
        return generateToken(email);
    }

    public String generateToken(String email) {
        Long expirationTime = Instant.now().toEpochMilli() + tokenExpireTime;
        String data = email +
                ":" +
                expirationTime +
                ":" +
                generateTokenSignature(email, expirationTime.toString(), key);
        return new String(Base64.getEncoder().encode(data.getBytes()));
    }

    protected String generateTokenSignature(String userId, String expirationTime, String key) {
        String data = userId + expirationTime + key;
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No MD5 algorithm available!");
        }
        return new String(Hex.encode(digest.digest(data.getBytes())));
    }


    protected String[] decodAndSplitToken(String token) {
        String docodedToken = new String(Base64.getDecoder().decode(token));
        return docodedToken.split(":");
    }

    @Override
    public boolean validateToken(String token) {
        String[] tokenParametrs = decodAndSplitToken(token);
        if (tokenParametrs.length != 3) return false;
        if (Instant.now().toEpochMilli() > Long.parseLong(tokenParametrs[1])) return false;
        String tokenSignature = generateTokenSignature(tokenParametrs[0], tokenParametrs[1], key);
        return tokenParametrs[2].equals(tokenSignature);
    }


    @Override
    public String getUserIdFromToken(String token) {
        String[] tokenParametrs = decodAndSplitToken(token);
        return tokenParametrs[0];
    }


}
