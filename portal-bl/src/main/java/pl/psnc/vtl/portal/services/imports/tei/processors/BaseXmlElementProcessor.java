package pl.psnc.vtl.portal.services.imports.tei.processors;

import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;
import pl.psnc.vtl.portal.services.imports.TextFormatValidator;

import java.util.*;

public abstract class BaseXmlElementProcessor {
    //processor state variables
    protected HashMap<String, XMLNodeElementHandler> nodesOperations =new HashMap<>();
    protected HashMap<String, List<XmlNode>> nodes = new HashMap<>();

    private  boolean isProcessingChildren = false;
    protected List<String> elementsToProcessWithChildren = new ArrayList<>();
    private  List<XmlNode> processedParentsNodes = new ArrayList<>();

    //methods for marking sections
    public abstract String getNodesMarker();


    public abstract void initNodeOperations();
    public abstract void initElementsToProcessWithChildren();

    public BaseXmlElementProcessor(){
        initNodeOperations();
        initElementsToProcessWithChildren();
    }

    private void setProcessingChildren(String nodeName, boolean isStarting){
        if (this.elementsToProcessWithChildren.contains(nodeName)) {
            this.isProcessingChildren = isStarting;
            if (!isStarting && !processedParentsNodes.isEmpty()){
                processedParentsNodes.remove( processedParentsNodes.size() - 1);
            }
        }
    }


    private void processNode(XmlNode xmlNode){
        if (nodesOperations.containsKey(xmlNode.getName())) {
            nodes.putIfAbsent(xmlNode.getName(), new ArrayList<>());
            nodes.get(xmlNode.getName()).add(xmlNode);
        }
    }

    private void processParentsAndChildren(XmlNode xmlNode){
        if (this.isProcessingChildren){
            for (XmlNode parentNode : this.processedParentsNodes){
                parentNode.getChildNodes().add(xmlNode);
            }
        }

        if (this.elementsToProcessWithChildren.contains(xmlNode.getName())) {
            xmlNode.setChildNodes(new ArrayList<>());
            this.processedParentsNodes.add(xmlNode);
            this.isProcessingChildren = true;
        }
    }

    private void performProcessingOfCollectedNodes(TranscriptionCore core){
        nodes.forEach( (keyName, nodesList) ->
                nodesList.forEach( xmlNode -> nodesOperations.get(keyName)
                        .handleElement(xmlNode, core)));
    }

    private void validateProcessorFinalState(){
        if (nodes.isEmpty()){
            TextFormatValidator.getWrongTypeXMLException();
        }
    }


    public void processElement(XmlNode xmlNode) {
         processNode(xmlNode);
         processParentsAndChildren(xmlNode);
    }

    public void processBeginOfElement(String nodeName) {
        setProcessingChildren(nodeName, true);
    }

    public void processEndOfElement(String nodeName) {
        setProcessingChildren(nodeName, false);
    }

    public final void processElements(TranscriptionCore core){
        validateProcessorFinalState();
        performProcessingOfCollectedNodes(core);
    };

}