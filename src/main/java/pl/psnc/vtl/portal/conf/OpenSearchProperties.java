package pl.psnc.vtl.portal.conf;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration class used to import values from properties to description.xml endpoint response
 */

@Getter
@PropertySource("classpath:/application.yml")
@Configuration
public class OpenSearchProperties {

    @Value("${opensearch.shortName}")
    private String shortName;

    @Value("${opensearch.description}")
    private String description;

    @Value("${opensearch.descriptionSummary}")
    private String descriptionSummary;

    @Value("${opensearch.tag}")
    private String tag;

    @Value("${opensearch.contact}")
    private String contact;

    @Value("${opensearch.port}")
    private String port;

    @Value("${opensearch.url}")
    private String url;

    @Value("${opensearch.longName}")
    private String longName;

    @Value("${opensearch.developer}")
    private String developer;

    @Value("${opensearch.attribution}")
    private String attribution;

    @Value("${opensearch.syndicationRight}")
    private String syndicationRight;

    @Value("${opensearch.adultContent}")
    private String adultContent;

    @Value("${opensearch.language}")
    private String language;

    @Value("${opensearch.outputEncoding}")
    private String outputEncoding;

    @Value("${opensearch.inputEncoding}")
    private String inputEncoding;

    @Value("${opensearch.fbcSearchAddinsFormat}")
    private String fbcSearchAddinsFormat;

    @Value("${opensearch.idUrlFragment}")
    private String idUrlFragment;

    public String getUrlWithPort() {
        return port.trim().isEmpty() || port.trim().isBlank() ? url : url + ":" + port;
    }

    public String getBackendUrl() {
        return url;
    }

    public String getIdUrlPrefix() {
        return getUrlWithPort() + "/" + idUrlFragment + "/";
    }
}

