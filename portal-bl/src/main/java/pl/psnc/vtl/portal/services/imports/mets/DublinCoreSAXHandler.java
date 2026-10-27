package pl.psnc.vtl.portal.services.imports.mets;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;
import pl.psnc.vtl.portal.services.imports.TextFormatValidator;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

class DublinCoreSAXHandler extends DefaultHandler {

    private boolean isElement = false;
    private String currentElement = new String();

    TranscriptionCore transcriptionCore = new TranscriptionCore();
    private Map<String, DublinCoreElements> dublinCoreElementsMap = new HashMap<>();
    //Due to order of summary items its cannot be hashMap
    private Map<String, String> valuesElements = new LinkedHashMap<>();

    DublinCoreSAXHandler(){
        EnumSet.allOf(DublinCoreElements.class).forEach(e -> dublinCoreElementsMap.put(e.getElementName(), e));
    }

    //in case no elements found what match DC specification user "probably" gave a wrong file type
    @Override
    public void endDocument(){
        if(valuesElements.isEmpty()) throw TextFormatValidator.getWrongTypeXMLException();
        valuesElements.forEach((v, e) ->
                dublinCoreElementsMap.get(e).handleElement(v, transcriptionCore));
    }

    @Override
    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts){
        if(dublinCoreElementsMap.containsKey(qName)){
            currentElement = qName;
            isElement = true;
        }
    }

    //Field Dc:Type must be processed in the first place due to other field setup depending on DC:Type
    @Override
    public void characters (char ch[], int start, int length)
    {
        if(isElement && currentElement.equals(DublinCoreElements.TYPE.getElementName())){
            dublinCoreElementsMap.get(currentElement).handleElement(new String(ch, start, length), transcriptionCore);
            isElement = false;
        }
        if(isElement) {
            valuesElements.put(new String(ch, start, length), currentElement);
            isElement = false;
        }
    }


}
