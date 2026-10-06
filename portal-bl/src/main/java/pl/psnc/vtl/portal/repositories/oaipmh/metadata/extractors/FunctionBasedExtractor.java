package pl.psnc.vtl.portal.repositories.oaipmh.metadata.extractors;

import lombok.RequiredArgsConstructor;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.oaipmh.metadata.DcValue;
import pl.psnc.vtl.portal.repositories.oaipmh.metadata.ValuesExtractor;

import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
public class FunctionBasedExtractor implements ValuesExtractor {
    private final Function<Transcription, List<DcValue>> extractionFunction;
    @Override
    public List<DcValue> extractValues(Transcription transcription) {
        return extractionFunction.apply(transcription);
    }
}
