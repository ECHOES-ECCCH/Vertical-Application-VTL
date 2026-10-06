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
public class TeiMonographElementMapper extends  TeiElementMapper{

    @Autowired
    public TeiMonographElementMapper(MappingConfiguration dumpObject) {
        super(dumpObject);
    }

    public static void setMonographTitle(XmlNode xmlNode, TranscriptionCore transcriptionCore){
        String title = xmlNode.getValue();
        transcriptionCore.getMonographicInfo().setTitle(title);
    }

    public static void setMonographAuthor(XmlNode xmlNode, TranscriptionCore transcriptionCore){
        XmlNode persNameNode = findChildByNodeName(xmlNode, "persName");
        if (persNameNode != null) {
            String name = concatenateChildrenContent(persNameNode, " ");
            ActorCore actorCore = new ActorCore();
            actorCore.setName(name);
            actorCore.setRole(configuration.getActorRoleId("author"));
            transcriptionCore.getMonographicInfo().getActors().add(actorCore);
        }
    }

    public static void setMonographEditor(XmlNode xmlNode, TranscriptionCore transcriptionCore){
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
            transcriptionCore.getMonographicInfo().getActors().add(actorCore);
        }
    }

    public static void setMonographLanguage(XmlNode xmlNode, TranscriptionCore transcriptionCore){
        String langCode = xmlNode.getAttribute("mainLang");
        //language should be set only one time!
        if (langCode != null && transcriptionCore.getMonographicInfo().getLanguage() == null){
            LanguageId lang = new LanguageId();
            lang.setCode(configuration.getLanguageCode(langCode));
            if (lang.getCode() != null)
                transcriptionCore.getMonographicInfo().setLanguage(lang);
        }
    }

    public static void setMonographBiblScopeData(XmlNode xmlNode, TranscriptionCore transcriptionCore){
        String unit = xmlNode.getAttribute("unit");
        Integer num = null;
        try{
            num = Integer.parseInt(xmlNode.getValue());
        }
        catch (Exception ignore){}

        if (unit != null && num != null){
            if ("chapter".equals(unit)){
                transcriptionCore.getMonographicInfo().setChapterNo(num);
            }
            if ("volume".equals(unit)){
                transcriptionCore.getMonographicInfo().setVolumeNo(num);
            }
            if ("issue".equals(unit)){
                transcriptionCore.getMonographicInfo().setIssueNo(num);
            }
        }

        String from = xmlNode.getAttribute("from");
        String to = xmlNode.getAttribute("to");
        String page = xmlNode.getAttribute("page");

        if (!StringUtils.isEmpty(from) || !StringUtils.isEmpty(from)){
            StringBuilder sb = new StringBuilder();
            if (from != null)
                sb.append(from);
            sb.append("-");
            if (to != null)
                sb.append(to);
            String pageRange = sb.toString();
            transcriptionCore.getMonographicInfo().setPageRange(pageRange);
        } else {
            try {
                if ("page".equals(unit)) {
                    Integer pages = Integer.parseInt(xmlNode.getValue());
                    transcriptionCore.getMonographicInfo().setPages(pages);
                }
            }
            catch (Exception ingore){}
        }
    }

    public static void setMonographPublisher(XmlNode xmlNode, TranscriptionCore transcriptionCore){
        String publisher = xmlNode.getValue();
        if (!StringUtils.isEmpty(publisher))
            transcriptionCore.getMonographicInfo().setPublisher(publisher);
    }

    public static void setMonographPublicationPlace(XmlNode xmlNode, TranscriptionCore transcriptionCore){
        String pubPlace = xmlNode.getValue();
        if (!StringUtils.isEmpty(pubPlace))
            transcriptionCore.getMonographicInfo().setPublicationPlace(pubPlace);
    }

    public static void setMonographPublicationDate(XmlNode xmlNode, TranscriptionCore transcriptionCore){
        String pubDate = xmlNode.getValue();
        String pubDateAttr = xmlNode.getAttribute("when");
        if (!StringUtils.isEmpty(pubDate))
            transcriptionCore.getMonographicInfo().setPublicationDate(pubDate);
        if (!StringUtils.isEmpty(pubDateAttr))
            transcriptionCore.getMonographicInfo().setPublicationDate(pubDateAttr);
    }

    public static void setMonographISSN(XmlNode xmlNode, TranscriptionCore transcriptionCore){
        String typeAttr = xmlNode.getAttribute("type");
        String idStr = xmlNode.getValue();
        if (StringUtils.isEmpty(typeAttr))
            return;
        typeAttr = typeAttr.toLowerCase();
        if ("isbn".equals(typeAttr))
            transcriptionCore.getMonographicInfo().setIsbn(idStr);
        if ("eisbn".equals(typeAttr))
            transcriptionCore.getMonographicInfo().setEisbn(idStr);
        if ("issn".equals(typeAttr))
            transcriptionCore.getMonographicInfo().setIssn(idStr);
        if ("eissn".equals(typeAttr))
            transcriptionCore.getMonographicInfo().setEissn(idStr);
    }

}
