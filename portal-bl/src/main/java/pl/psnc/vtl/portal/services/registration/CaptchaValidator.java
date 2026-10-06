package pl.psnc.vtl.portal.services.registration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class CaptchaValidator {

    @Value("${captcha.secret-key}")
    private String secretKey;

    @Value("${captcha.score-threshold:0.5}")
    private float scoreThreshold;

    private final String GOOGLE_RECAPTCHA_ENDPOINT = "https://www.google.com/recaptcha/api/siteverify";
    final RestTemplate restTemplate;

    public CaptchaValidator() {
        this.restTemplate = new RestTemplate();
    }

    public boolean isValidCaptcha(String captcha) {
        try {
            MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
            requestMap.add("secret", secretKey);
            requestMap.add("response", captcha);

            JsonNode responseMap = restTemplate.postForObject(GOOGLE_RECAPTCHA_ENDPOINT, requestMap, JsonNode.class);

            log.debug("reCAPTCHA response: {}", responseMap);

            if (!responseMap.has("success") || !responseMap.get("success").asBoolean()) {
                log.warn("reCAPTCHA verification failed: {}",
                        responseMap.has("error-codes") ? responseMap.get("error-codes").toString() : "unknown error");
                return false;
            }

            if (responseMap.has("score")) {
                float score = responseMap.get("score").floatValue();
                log.debug("reCAPTCHA score: {}", score);

                return score >= scoreThreshold;
            }
            return true;

        } catch (Exception e) {
            log.error("Error validating reCAPTCHA", e);
            return false;
        }
    }
}