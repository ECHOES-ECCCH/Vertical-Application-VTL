package pl.psnc.vtl.portal.services.imports.tei.mappers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;
import pl.psnc.vtl.portal.services.imports.MappingConfiguration;
import pl.psnc.vtl.portal.services.imports.tei.processors.XmlNode;

@Slf4j
@Service
public class TeiProfileElementMapper extends TeiElementMapper{

    @Autowired
    public TeiProfileElementMapper(MappingConfiguration dumpObject) {
        super(dumpObject);
    }

    public static void setProfileKeyword(XmlNode xmlNode, TranscriptionCore transcriptionCore) {
        String keyword = xmlNode.getValue();
        transcriptionCore.getProfileDesc().getKeywords().add(keyword);
    }

    public static void setProfileSummary(XmlNode xmlNode, TranscriptionCore transcriptionCore) {
        String summary = concatenateChildrenContent(xmlNode);
        StringBuilder sb = new StringBuilder();
        if (transcriptionCore.getProfileDesc().getSummary() != null) {
            sb.append(transcriptionCore.getProfileDesc().getSummary());
            sb.append(System.lineSeparator());
        }
        sb.append(summary);
        transcriptionCore.getProfileDesc().setSummary(sb.toString());
    }

}
