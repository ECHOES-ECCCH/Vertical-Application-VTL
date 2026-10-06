package pl.psnc.vtl.portal.repositories.oaipmh.metadata.extractors;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.oaipmh.metadata.DcValue;
import pl.psnc.vtl.portal.repositories.oaipmh.metadata.ValuesExtractor;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SubjectExtractor implements ValuesExtractor {
    @Override
    public List<DcValue> extractValues(Transcription transcription) {
        if (transcription.getProfileDesc() == null || transcription.getProfileDesc().getKeywords() == null) {
            return List.of();
        }
        return transcription.getProfileDesc().getKeywords().stream().filter(Strings::isNotBlank)
                .map(keyword -> new DcValue(keyword, determineLanguage(transcription.getLastSetLanguageCode())))
                .collect(Collectors.toList());
    }
}
