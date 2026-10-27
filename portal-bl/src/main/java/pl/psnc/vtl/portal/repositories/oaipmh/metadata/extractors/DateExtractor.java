package pl.psnc.vtl.portal.repositories.oaipmh.metadata.extractors;

import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.oaipmh.metadata.DcValue;
import pl.psnc.vtl.portal.repositories.oaipmh.metadata.ValuesExtractor;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class DateExtractor implements ValuesExtractor {
    @Override
    public List<DcValue> extractValues(Transcription transcription) {
        if (Objects.nonNull(transcription.getDateCreated())) {
            return List.of(new DcValue(transcription.getDateCreated().format(DateTimeFormatter.ISO_DATE),
                    determineLanguage(transcription.getLastSetLanguageCode())));
        }
        return List.of();
    }
}
