package pl.psnc.vtl.portal.repositories.oaipmh.metadata.extractors;

import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.oaipmh.metadata.DcValue;
import pl.psnc.vtl.portal.repositories.oaipmh.metadata.ValuesExtractor;

import java.util.List;
import java.util.Objects;

public class SourceExtractor implements ValuesExtractor {
    @Override
    public List<DcValue> extractValues(Transcription transcription) {
        if (Objects.nonNull(transcription.getExternalUrl())) {
            return List.of(new DcValue(transcription.getExternalUrl(),
                    determineLanguage(transcription.getLastSetLanguageCode())));
        }
        return List.of();
    }
}
