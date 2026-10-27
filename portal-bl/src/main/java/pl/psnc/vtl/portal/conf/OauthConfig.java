package pl.psnc.vtl.portal.conf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OauthConfig {

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Client {
        @Value("client-id")
        String clientId;
        @Value("redirect-uri")
        String redirectUri;
    }

    private Map<String, Client> registration = new HashMap<>();
}