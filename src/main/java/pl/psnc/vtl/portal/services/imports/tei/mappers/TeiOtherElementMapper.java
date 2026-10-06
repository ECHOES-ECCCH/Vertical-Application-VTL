package pl.psnc.vtl.portal.services.imports.tei.mappers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;
import pl.psnc.vtl.portal.services.imports.MappingConfiguration;
import pl.psnc.vtl.portal.services.imports.tei.processors.XmlNode;

@Slf4j
@Service
public class TeiOtherElementMapper extends  TeiElementMapper{

    @Autowired
    public TeiOtherElementMapper(MappingConfiguration dumpObject) {
        super(dumpObject);
    }

    public static void setExternalId(XmlNode xmlNode, TranscriptionCore transcriptionCore){
       XmlNode repositoryNode = findChildByNodeName(xmlNode, "repository");
       XmlNode idnoNode = findChildByNodeName(xmlNode, "idno");
       if (repositoryNode != null && idnoNode != null){
           String externalId = repositoryNode.getValue() + " " + idnoNode.getValue();
           transcriptionCore.setExternalId(externalId);
       }
    }
}
