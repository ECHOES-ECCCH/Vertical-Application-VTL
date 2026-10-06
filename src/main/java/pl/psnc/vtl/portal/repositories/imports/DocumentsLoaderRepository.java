package pl.psnc.vtl.portal.repositories.imports;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.services.imports.URLValidator;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Repository
@Slf4j
public class DocumentsLoaderRepository {

    @Value("${export.trust-self-signed-certificates}")
    private boolean trustSelfSignedCertificates;

    private CloseableHttpClient client;

    @PostConstruct
    public void init() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        if (trustSelfSignedCertificates) {
            try {
                SSLContext sslContext =
                        SSLContextBuilder.create().loadTrustMaterial(new TrustSelfSignedStrategy()).build();
                builder.setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier()));
            } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
                throw new RuntimeException(e);
            }
        }
        client = builder.build();
    }


    public String loadDocumentStringFromResource(String url) {
        url = handleLinksWithoutProtocol(url);
        URLValidator.validate(url);
        try (CloseableHttpResponse response = client.execute(new HttpGet(url))) {
            URLValidator.validateResponse(response);
            return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.toString());
        } catch (IOException e) {
            log.error("Could not load resource: " + url, e);
            throw URLValidator.getNotFoundValidationException();
        }
    }


    /**
     * Links in Univeristy of Wroclaw IIIF viewer DO NOT have protocol. In this case it has to be added.
     * @param url link
     * @return link with http suffix, if original value doesn't start with http
     */
    private String handleLinksWithoutProtocol(String url){
        if(url.startsWith("//"))
            return "http:".concat(url);
        return url;
    }

}
