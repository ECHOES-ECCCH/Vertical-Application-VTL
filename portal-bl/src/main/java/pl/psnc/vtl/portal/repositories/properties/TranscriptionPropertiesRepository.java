package pl.psnc.vtl.portal.repositories.properties;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.services.properties.LoadPropertiesException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static java.util.stream.Collectors.toList;

@Service
public class TranscriptionPropertiesRepository {

    @Value("${vtl.conf-dir}")
    private String configDir;

    private Properties properties;

    private final String MAPPING_PROPERTIES = "transcription.properties";

    public List<String> getProperties(String propertiesName) {
        if (properties == null) {
            try (InputStream input = new FileInputStream(configDir.concat(MAPPING_PROPERTIES))) {
                Properties p = new Properties();
                p.load(input);
                properties = p;
            } catch (IOException ex) {
                throw new LoadPropertiesException("Error while loading transcription.properties", ex);
            }
        }
        return Arrays.stream(properties.getProperty(propertiesName, "").split(","))
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(toList());
    }
}
