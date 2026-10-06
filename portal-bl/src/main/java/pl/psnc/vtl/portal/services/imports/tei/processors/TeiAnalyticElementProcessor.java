package pl.psnc.vtl.portal.services.imports.tei.processors;

import pl.psnc.vtl.portal.services.imports.tei.mappers.TeiAnalyticElementMapper;

public class TeiAnalyticElementProcessor extends BaseXmlElementProcessor {

    private static final String ANALYTIC_NODES_MARKER = "analytic";


    @Override
    public String getNodesMarker() {
        return  ANALYTIC_NODES_MARKER;
    }

    @Override
    public void initNodeOperations() {
        this.nodesOperations.put("title", (v, c) -> TeiAnalyticElementMapper.setAnalyticTitle(v, c));
        this.nodesOperations.put("author", (v, c) -> TeiAnalyticElementMapper.setAnalyticAuthor(v, c));
        this.nodesOperations.put("editor", (v, c) -> TeiAnalyticElementMapper.setAnalyticEditor(v, c));
        this.nodesOperations.put("persName", (v, c) -> TeiAnalyticElementMapper.setEmptyHandler(v, c));
        this.nodesOperations.put("forename", (v, c) -> TeiAnalyticElementMapper.setEmptyHandler(v, c));
        this.nodesOperations.put("surname", (v, c) -> TeiAnalyticElementMapper.setEmptyHandler(v, c));
        this.nodesOperations.put("email", (v, c) -> TeiAnalyticElementMapper.setEmptyHandler(v, c));
        this.nodesOperations.put("textLang", (v, c) -> TeiAnalyticElementMapper.setAnalyticLanguage(v, c));
    }

    @Override
    public void initElementsToProcessWithChildren() {
        this.elementsToProcessWithChildren.add("author");
        this.elementsToProcessWithChildren.add("editor");
        this.elementsToProcessWithChildren.add("persName");
        this.elementsToProcessWithChildren.add("abstract");
    }

}
