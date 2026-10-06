package pl.psnc.vtl.portal.repositories.oaipmh.metadata.extractors;

import lombok.RequiredArgsConstructor;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.oaipmh.OaiItemUtils;
import pl.psnc.vtl.portal.repositories.oaipmh.metadata.DcValue;
import pl.psnc.vtl.portal.repositories.oaipmh.metadata.ValuesExtractor;

import java.util.List;

@RequiredArgsConstructor
public class ConfigurationBasedExtractor implements ValuesExtractor {

    private final String fieldName;


    @Override
    public List<DcValue> extractValues(Transcription transcription) {
        return OaiItemUtils.getDcFieldConfiguration(fieldName);
    }
}
