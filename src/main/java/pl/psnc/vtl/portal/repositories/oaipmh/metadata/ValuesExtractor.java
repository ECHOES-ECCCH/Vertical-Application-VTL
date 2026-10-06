package pl.psnc.vtl.portal.repositories.oaipmh.metadata;

import org.apache.logging.log4j.util.Strings;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.oaipmh.OaiItemUtils;

import java.util.List;

public interface ValuesExtractor {

    List<DcValue> extractValues(Transcription transcription);

    default String determineLanguage(String languageCode) {
        if (Strings.isNotBlank(languageCode)) {
            return OaiItemUtils.mapToXmlLang(languageCode);
        }
        return Strings.EMPTY;
    }
}
