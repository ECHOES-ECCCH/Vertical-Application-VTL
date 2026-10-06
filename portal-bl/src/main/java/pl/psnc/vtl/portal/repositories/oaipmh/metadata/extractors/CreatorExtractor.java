package pl.psnc.vtl.portal.repositories.oaipmh.metadata.extractors;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.oaipmh.metadata.DcValue;
import pl.psnc.vtl.portal.repositories.oaipmh.metadata.ValuesExtractor;

import java.util.List;

@Component
public class CreatorExtractor implements ValuesExtractor {
    @Override
    public List<DcValue> extractValues(Transcription transcription) {
        if (Strings.isNotBlank(transcription.getAuthorsLabel())) {
            return List.of(new DcValue(transcription.getAuthorsLabel(),
                    determineLanguage(transcription.getLastSetLanguageCode())));
        }
        return List.of();
    }
}
