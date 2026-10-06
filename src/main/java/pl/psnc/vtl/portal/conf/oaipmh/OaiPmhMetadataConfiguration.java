package pl.psnc.vtl.portal.conf.oaipmh;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@EnableConfigurationProperties
@ConfigurationProperties("oai-repository.record-metadata")
@Component
@Getter
@Setter
public class OaiPmhMetadataConfiguration {
    private Map<String, Map<String, String>> fieldValues;
    private Map<String, String> langCodeToXmlLang;

    public String mapToXmlLang(String langCode) {
        return langCodeToXmlLang != null ? langCodeToXmlLang.getOrDefault(langCode, Strings.EMPTY) : Strings.EMPTY;
    }

    public Map<String, String> getXmlLangToValueMapForField(String fieldName) {
        return fieldValues.get(fieldName);
    }
}
