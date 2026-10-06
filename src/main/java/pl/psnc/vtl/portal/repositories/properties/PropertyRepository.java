package pl.psnc.vtl.portal.repositories.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.services.properties.LoadPropertiesException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Repository
public class PropertyRepository {

    @Value("${vtl.conf-dir}")
    private String configDir;

    private final String VLT_PROPERTIES = "vtl.properties";

    public Properties loadProperties() {
        Properties properties = new Properties();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(configDir + VLT_PROPERTIES),
                StandardCharsets.UTF_8))) {
            properties.load(br);
            return properties;
        } catch (IOException ex) {
            throw new LoadPropertiesException("Error while loading vtl.properties", ex);
        }
    }

}
