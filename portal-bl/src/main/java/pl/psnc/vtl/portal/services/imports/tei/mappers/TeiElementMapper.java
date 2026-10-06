package pl.psnc.vtl.portal.services.imports.tei.mappers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;
import pl.psnc.vtl.portal.services.imports.MappingConfiguration;
import pl.psnc.vtl.portal.services.imports.tei.processors.XmlNode;

import javax.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public abstract class TeiElementMapper {

    private final MappingConfiguration dumpObject;

    @PostConstruct
    private void initMappingConfiguration(){
        configuration = dumpObject;
    }

    protected static MappingConfiguration configuration;

    protected static XmlNode findChildByNodeName(XmlNode xmlNode, String childName){

        if (xmlNode.getChildNodes() == null || childName == null)
            return  null;

        for (XmlNode childNode : xmlNode.getChildNodes()){
            if (childName.equals(childNode.getName()))
                return  childNode;
        }

        return null;
    }

    // ------------------ common methods ---------------------------------------------
    // -------------------------------------------------------------------------------
    public static void setEmptyHandler(XmlNode xmlNode, TranscriptionCore transcriptionCore){
    }


    protected static String concatenateChildrenContent(XmlNode xmlNode, String separator){
        StringBuilder sb = new StringBuilder();
        sb.append(xmlNode.getValue());
        if (xmlNode.getChildNodes() != null){
            xmlNode.getChildNodes().forEach(xn -> {sb.append(separator); sb.append(xn.getValue()); });
        }
        return sb.toString().trim();
    }

    protected static String concatenateChildrenContent(XmlNode xmlNode) {
        return concatenateChildrenContent(xmlNode, System.lineSeparator());
    }
}
