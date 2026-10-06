package pl.psnc.vtl.portal.repositories.oaipmh.metadata.extractors;

import org.apache.logging.log4j.util.Strings;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.oaipmh.metadata.DcValue;
import pl.psnc.vtl.portal.repositories.oaipmh.metadata.ValuesExtractor;

import java.util.List;

public class DescriptionExtractor implements ValuesExtractor {
    @Override
    public List<DcValue> extractValues(Transcription transcription) {
        if (transcription.getProfileDesc() == null || Strings.isBlank(transcription.getProfileDesc().getSummary())) {
            return List.of();
        }
        return List.of(new DcValue(transcription.getProfileDesc().getSummary(),
                determineLanguage(transcription.getLastSetLanguageCode())));
    }
}
