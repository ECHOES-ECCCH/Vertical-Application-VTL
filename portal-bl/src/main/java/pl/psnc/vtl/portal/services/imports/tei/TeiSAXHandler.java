package pl.psnc.vtl.portal.services.imports.tei;

import lombok.Getter;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;
import pl.psnc.vtl.portal.services.imports.tei.initializers.TranscriptionCoreInitializer;
import pl.psnc.vtl.portal.services.imports.tei.processors.*;

import java.util.HashMap;

public class TeiSAXHandler extends DefaultHandler {

    //state variables
    boolean isElement = false;
    private String currentElement = null;
    private Attributes currentAttributes = null;
    private BaseXmlElementProcessor currentProcessor = null;
    private BaseXmlElementProcessor othersProcessor = null;


    private HashMap<String, BaseXmlElementProcessor> teiXmlElementProcessors;

    @Getter
    private TranscriptionCore transcriptionCore = new TranscriptionCore();


    @Override
    public void startDocument() {
        TranscriptionCoreInitializer transcriptionCoreInitializer = new TranscriptionCoreInitializer();
        transcriptionCoreInitializer.initialize(this.transcriptionCore);

        this.othersProcessor = new TeiOthersElementProcessor();

        TeiAnalyticElementProcessor teiAnalyticElementProcessor = new TeiAnalyticElementProcessor();
        TeiMonographElementProcessor teiMonographElementProcessor = new TeiMonographElementProcessor();
        TeiProfileElementProcessor teiProfileElementProcessor = new TeiProfileElementProcessor();
        TeiTitleElementProcessor teiTitleElementProcessor = new TeiTitleElementProcessor();
        teiXmlElementProcessors = new HashMap<>();
        teiXmlElementProcessors.put(teiAnalyticElementProcessor.getNodesMarker(), teiAnalyticElementProcessor);
        teiXmlElementProcessors.put(teiMonographElementProcessor.getNodesMarker(), teiMonographElementProcessor);
        teiXmlElementProcessors.put(teiProfileElementProcessor.getNodesMarker(), teiProfileElementProcessor);
        teiXmlElementProcessors.put(teiTitleElementProcessor.getNodesMarker(), teiTitleElementProcessor);

        //by default we are processing non section elements (called: others)
        currentProcessor = othersProcessor;
    }

    @Override
    public void endDocument(){
        teiXmlElementProcessors.put(othersProcessor.getNodesMarker(), othersProcessor);
        teiXmlElementProcessors.values().forEach(p -> p.processElements(transcriptionCore));
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes){

        //check if we have begin of one from processed sections
        currentProcessor = teiXmlElementProcessors.getOrDefault(localName, currentProcessor);
        currentProcessor.processBeginOfElement(localName);

        //preserve element to (potential) processing in characters()
        isElement = true;
        currentElement = localName;
        currentAttributes = attributes;
    }

    @Override
    public void endElement(String uri, String localName, String qName){
        currentProcessor.processEndOfElement(localName);

        //check if we have end of one from processed sections
        if (localName.equals(currentProcessor.getNodesMarker())){
            //we will be processing others (non section) elements
            currentProcessor = othersProcessor;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length){
        if (isElement) {
            currentProcessor.processElement(
                    new XmlNode(currentElement, new String(ch, start, length), currentAttributes, null));
            isElement = false;
        }
    }
}
