package pl.psnc.vtl.portal.services.imports.tei.processors;

import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;

@FunctionalInterface
public interface XMLNodeElementHandler {
    void handleElement(XmlNode xmlNode, TranscriptionCore core);
}