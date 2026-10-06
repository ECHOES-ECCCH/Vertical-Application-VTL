package pl.psnc.vtl.portal.services.imports;

import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;

public interface MapperStrategy {

    TranscriptionCore mapToImportTemplate(String content);

}
