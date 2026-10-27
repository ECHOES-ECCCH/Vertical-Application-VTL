package pl.psnc.vtl.portal.services.imports.tei.processors;

import pl.psnc.vtl.portal.services.imports.tei.mappers.TeiAnalyticElementMapper;

public class TeiTitleElementProcessor extends BaseXmlElementProcessor {

    private static final String TITLE_NODES_MARKER = "titleStmt";


    @Override
    public String getNodesMarker() {
        return  TITLE_NODES_MARKER;
    }

    @Override
    public void initNodeOperations() {
        //Puts global title into analyticInfo title cause ui uses this one
        this.nodesOperations.put("title", (v, c) -> TeiAnalyticElementMapper.setAnalyticTitle(v, c));
    }

    @Override
    public void initElementsToProcessWithChildren() {
    }

}
