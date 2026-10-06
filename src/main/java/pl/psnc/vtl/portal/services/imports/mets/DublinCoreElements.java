package pl.psnc.vtl.portal.services.imports.mets;

import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;

enum DublinCoreElements implements XMLElementHandler {
    TITLE("dc:title", (a, c) -> DublinCoreElementMapper.setTitle(a, c)),
    CREATOR("dc:creator", (a, c) -> DublinCoreElementMapper.setCreator(a, c)),
    RELATION("dc:relation", (a, c) -> DublinCoreElementMapper.setRelation(a, c)),
    CONTRIBUTOR("dc:contributor", (a, c) -> DublinCoreElementMapper.setContributor(a, c)),
    DATE("dc:date", (a, c) -> DublinCoreElementMapper.setDate(a, c)),
    DESCRIPTION("dc:description", (a, c) -> DublinCoreElementMapper.setDescription(a, c)),
    SUBJECT("dc:subject", (a, c) -> DublinCoreElementMapper.setSubject(a, c)),
    IDENTIFIER("dc:identifier", (a, c) -> DublinCoreElementMapper.setIdentifier(a, c)),
    PUBLISHER("dc:publisher", (a, c) -> DublinCoreElementMapper.setPublisher(a, c)),
    LANGUAGE("dc:language", (a, c) -> DublinCoreElementMapper.setLanguage(a, c)),
    TYPE("dc:type", (a, c) -> DublinCoreElementMapper.setAnalytic(a, c));

    private final String elementName;
    private final XMLElementHandler handler;

    DublinCoreElements(String elementName, XMLElementHandler handler) {
        this.elementName = elementName;
        this.handler = handler;
    }

    String getElementName() {
        return elementName;
    }

    public TranscriptionCore handleElement(String elementValue, TranscriptionCore core) {
        return handler.handleElement(elementValue, core);
    }
}