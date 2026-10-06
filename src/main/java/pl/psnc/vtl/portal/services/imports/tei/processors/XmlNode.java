package pl.psnc.vtl.portal.services.imports.tei.processors;

import lombok.Data;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
public class XmlNode {
    private  String name;
    private  String value;
    private HashMap<String, String> attributes = new HashMap<>();
    List<XmlNode> childNodes = new ArrayList<>();

    public XmlNode(String name, String value, Attributes saxAttributes, List<XmlNode> childNodes) {
        this.name = name;
        this.value = value;
        this.childNodes = childNodes;
        setAttributes(saxAttributes);
    }

    private void setAttributes(Attributes saxAttributes) {
        for (int i = 0; i < saxAttributes.getLength(); i++){
            attributes.put(saxAttributes.getQName(i), saxAttributes.getValue(i));
        }
    }

    public String getAttribute(String qName){
        return attributes.getOrDefault(qName, null);
    }

}
