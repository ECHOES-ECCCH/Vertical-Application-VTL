package pl.psnc.vtl.portal.services.imports.mets;

import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;

@FunctionalInterface
public interface XMLElementHandler {
    TranscriptionCore handleElement(String elementValue, TranscriptionCore core);
}
