package pl.psnc.vtl.portal.services.imports.tei.mappers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.dto.transcriptions.ActorCore;
import pl.psnc.vtl.portal.dto.transcriptions.LanguageId;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;
import pl.psnc.vtl.portal.services.imports.MappingConfiguration;
import pl.psnc.vtl.portal.services.imports.tei.processors.XmlNode;

@Slf4j
@Service
public class TeiAnalyticElementMapper extends TeiElementMapper {


    @Autowired
    public TeiAnalyticElementMapper(MappingConfiguration dumpObject) {
        super(dumpObject);
    }

    public static void setAnalyticTitle(XmlNode xmlNode, TranscriptionCore transcriptionCore){
        if (StringUtils.isBlank(transcriptionCore.getAnalyticInfo().getTitle())){
            //we need only first title in analytic
            String title = xmlNode.getValue();
            transcriptionCore.getAnalyticInfo().setTitle(title);
        }
    }

    public static void setAnalyticAuthor(XmlNode xmlNode, TranscriptionCore transcriptionCore){
        XmlNode persNameNode = findChildByNodeName(xmlNode, "persName");
        if (persNameNode != null) {
            String name = concatenateChildrenContent(persNameNode, " ");
            ActorCore actorCore = new ActorCore();
            actorCore.setName(name);
            actorCore.setRole(configuration.getActorRoleId("author"));
            transcriptionCore.getAnalyticInfo().getActors().add(actorCore);
        }
    }

    public static void setAnalyticEditor(XmlNode xmlNode, TranscriptionCore transcriptionCore){
        XmlNode persNameNode = findChildByNodeName(xmlNode, "persName");
        if (persNameNode != null) {
            String name = concatenateChildrenContent(persNameNode, " ");
            ActorCore actorCore = new ActorCore();
            actorCore.setName(name);
            try {
                String role = xmlNode.getAttribute("role");
                actorCore.setRole(configuration.getActorRoleId(role));
            }
            catch (Exception ignore){}
            transcriptionCore.getAnalyticInfo().getActors().add(actorCore);
        }
    }

    public static void setAnalyticLanguage(XmlNode xmlNode, TranscriptionCore transcriptionCore){
        String langCode = xmlNode.getAttribute("mainLang");
        //language should be set only one time!
        if (langCode != null && transcriptionCore.getAnalyticInfo().getLanguage() == null){
            LanguageId lang = new LanguageId();
            lang.setCode(configuration.getLanguageCode(langCode));
            if (lang.getCode() != null) {
                transcriptionCore.getAnalyticInfo().setLanguage(lang);
            }
        }
    }

}
