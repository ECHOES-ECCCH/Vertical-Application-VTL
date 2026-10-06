package pl.psnc.vtl.portal.repositories.oaipmh.metadata.extractors;

import org.apache.logging.log4j.util.Strings;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.oaipmh.metadata.DcValue;
import pl.psnc.vtl.portal.repositories.oaipmh.metadata.ValuesExtractor;

import java.util.List;
import java.util.Objects;

public class LanguageExtractor implements ValuesExtractor {
    @Override
    public List<DcValue> extractValues(Transcription transcription) {
        if (Objects.nonNull(transcription.getLastSetLanguageCode())) {
            return List.of(new DcValue(transcription.getLastSetLanguageCode(), Strings.EMPTY));
        }
        return List.of();
    }
}
