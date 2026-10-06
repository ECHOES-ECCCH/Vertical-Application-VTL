package pl.psnc.vtl.portal.services.properties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.repositories.properties.PropertyRepository;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;

    private final String SURVEY_URL_PROPERTY = "survey-url";
    private final String SYMBOLS_PROPERTY = "symbols";
    private final String DEFAULT_LANGUAGE_PROPERTY = "default-transcription-language";


    public String getSurveyURL() {
        String url = propertyRepository.loadProperties().getProperty(SURVEY_URL_PROPERTY);
        log.debug("Returning survey-url from vtl.properties: {}", url);
        return url;
    }

    public List<String> getSymbols() {
        String symbols = propertyRepository.loadProperties().getProperty(SYMBOLS_PROPERTY);
        return Arrays.asList(symbols.split(","));
    }

    public String getDefaultTranscriptionLanguage() {
        return propertyRepository.loadProperties().getProperty(DEFAULT_LANGUAGE_PROPERTY);
    }


}
