package pl.psnc.vtl.portal.conf.startup;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@UtilityClass
@Slf4j
public class YamlLoader {

    public List<Map<String, List<Object>>> loadYamlData(String filepath) {
        return loadYamlData(Collections.singletonList(filepath));
    }

    public List<Map<String, List<Object>>> loadYamlData(List<String> filepaths) {
        List<Map<String, List<Object>>> data = new ArrayList<>();
        ClassLoader classLoader = YamlLoader.class.getClassLoader();
        for (String filepath: filepaths) {
            try (InputStream dataStream = classLoader.getResourceAsStream(filepath)) {
                if (dataStream != null) {
                    data.add(new Yaml(new CustomClassLoaderConstructor(classLoader)).load(dataStream));
                } else {
                    log.info("File " + filepath + " not found, skipping data loading.");
                }
            } catch (IOException e) {
                log.error("Problem with file " + filepath, e);
            }
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getObjects(List<Map<String, List<Object>>> data, String label) {
        if (data == null) {
            return Collections.emptyList();
        }
        List<Object> objects = new ArrayList<>();
        for (Map<String, List<Object>> d : data) {
            if (d != null && d.get(label) != null) {
                objects.addAll(d.get(label));
            }
        }
        return (List<T>) objects;
    }

}
