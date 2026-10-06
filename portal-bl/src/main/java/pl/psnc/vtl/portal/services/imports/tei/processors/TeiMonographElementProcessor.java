package pl.psnc.vtl.portal.services.imports.tei.processors;

import pl.psnc.vtl.portal.services.imports.tei.mappers.TeiMonographElementMapper;

public class TeiMonographElementProcessor extends BaseXmlElementProcessor {

    private static final String MONOGRAPH_NODES_MARKER = "monogr";

    @Override
    public String getNodesMarker() {
        return MONOGRAPH_NODES_MARKER;
    }

    @Override
    public void initNodeOperations() {
        this.nodesOperations.put("title", (v, c) -> TeiMonographElementMapper.setMonographTitle(v, c));
        this.nodesOperations.put("author", (v, c) -> TeiMonographElementMapper.setMonographAuthor(v, c));
        this.nodesOperations.put("editor", (v, c) -> TeiMonographElementMapper.setMonographEditor(v, c));
        this.nodesOperations.put("persName", (v, c) -> TeiMonographElementMapper.setEmptyHandler(v, c));
        this.nodesOperations.put("forename", (v, c) -> TeiMonographElementMapper.setEmptyHandler(v, c));
        this.nodesOperations.put("surname", (v, c) -> TeiMonographElementMapper.setEmptyHandler(v, c));
        this.nodesOperations.put("email", (v, c) -> TeiMonographElementMapper.setEmptyHandler(v, c));
        this.nodesOperations.put("textLang", (v, c) -> TeiMonographElementMapper.setMonographLanguage(v, c));
        this.nodesOperations.put("biblScope", (v, c) -> TeiMonographElementMapper.setMonographBiblScopeData(v, c));
        this.nodesOperations.put("publisher", (v, c) -> TeiMonographElementMapper.setMonographPublisher(v, c));
        this.nodesOperations.put("pubPlace", (v, c) -> TeiMonographElementMapper.setMonographPublicationPlace(v, c));
        this.nodesOperations.put("date", (v, c) -> TeiMonographElementMapper.setMonographPublicationDate(v, c));
        this.nodesOperations.put("idno", (v, c) -> TeiMonographElementMapper.setMonographISSN(v, c));
    }

    @Override
    public void initElementsToProcessWithChildren() {
        this.elementsToProcessWithChildren.add("author");
        this.elementsToProcessWithChildren.add("editor");
        this.elementsToProcessWithChildren.add("persName");
        this.elementsToProcessWithChildren.add("abstract");
        this.elementsToProcessWithChildren.add("msIdentifier");
    }
}
