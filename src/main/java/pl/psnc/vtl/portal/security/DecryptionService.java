package pl.psnc.vtl.portal.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

@Component
@Configuration
@Slf4j
public class DecryptionService {

    @Value("${vtl.auth.private-key-filename}")
    private String privateKeyFilename;

    @Value("${vtl.conf-dir}")
    private String configDir;

    private PrivateKey privateKey;

    @PostConstruct
    public void init() {
        privateKey = initPrivateKey();
    }

    private byte[] loadPEM(String resource) throws IOException {
        InputStream in = new FileInputStream(resource);
        String pem = new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
        pem = pem.lines().filter(line -> !line.equals("-----BEGIN PRIVATE KEY-----") && !line.equals(
                "-----END PRIVATE KEY-----")).collect(
                Collectors.joining());
//                Collectors.joining("\n"));
//		pem = pem
//				.replace("-----BEGIN PRIVATE KEY-----\n", "")
//				.replace("-----END PRIVATE KEY-----\n", "");

        return Base64.getMimeDecoder().decode(pem);

    }

    private PrivateKey initPrivateKey() {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(loadPEM(configDir + privateKeyFilename));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Error during private key initialization", e);
            throw new IllegalArgumentException("Private key could not be initialized", e);
        }
    }

    public String decryptValue(String value) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(cipher.doFinal(Base64.getMimeDecoder().decode(value)), "UTF-8");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
            log.error(value + " could not be properly decrypted.", e);
        }
        return null;
    }
}
