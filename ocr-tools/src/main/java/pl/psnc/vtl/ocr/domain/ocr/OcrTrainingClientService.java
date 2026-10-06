package pl.psnc.vtl.ocr.domain.ocr;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static pl.psnc.vtl.ocr.conf.logging.Slf4jMDCFilter.DEFAULT_MDC_UUID_TOKEN_KEY;
import static pl.psnc.vtl.ocr.conf.logging.Slf4jMDCFilter.DEFAULT_RESPONSE_TOKEN_HEADER;

@Service
public class OcrTrainingClientService {

    @Value("${vtl.ocr-training.endpoint}")
    private String ocrTrainingEndpoint;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<String> getTrainableLanguages() {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(DEFAULT_RESPONSE_TOKEN_HEADER, MDC.get(DEFAULT_MDC_UUID_TOKEN_KEY));
            HttpEntity<?> entity = new HttpEntity<>(httpHeaders);
            return restTemplate
                    .exchange(ocrTrainingEndpoint + "/api/training/ocr/languages", HttpMethod.GET, entity, List.class)
                    .getBody();
        } catch (RestClientException ex) {
            throw new RuntimeException(ex);
        }
    }
}
