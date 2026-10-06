package pl.psnc.vtl.portal.services.imports.tei.processors;

import pl.psnc.vtl.portal.services.imports.tei.mappers.TeiProfileElementMapper;

public class TeiProfileElementProcessor extends BaseXmlElementProcessor {

    private static final String PROFILE_NODES_MARKER = "profileDesc";

    @Override
    public String getNodesMarker() {
        return PROFILE_NODES_MARKER;
    }

    @Override
    public void initNodeOperations() {
        this.nodesOperations.put("term", (v, c) -> TeiProfileElementMapper.setProfileKeyword(v, c));
        this.nodesOperations.put("abstract", (v, c) -> TeiProfileElementMapper.setProfileSummary(v, c));
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
