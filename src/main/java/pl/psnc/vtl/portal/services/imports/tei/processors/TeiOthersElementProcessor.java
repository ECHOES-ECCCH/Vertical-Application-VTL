package pl.psnc.vtl.portal.services.imports.tei.processors;

import pl.psnc.vtl.portal.services.imports.tei.mappers.TeiOtherElementMapper;
import pl.psnc.vtl.portal.services.imports.tei.mappers.TeiProfileElementMapper;

public class TeiOthersElementProcessor extends BaseXmlElementProcessor {

    @Override
    public String getNodesMarker() {
        return "TEI_OTHER";
    }

    @Override
    public void initNodeOperations() {
        this.nodesOperations.put("summary", (v, c) -> TeiProfileElementMapper.setProfileSummary(v, c));
        this.nodesOperations.put("msIdentifier", (v, c) -> TeiOtherElementMapper.setExternalId(v, c));
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
