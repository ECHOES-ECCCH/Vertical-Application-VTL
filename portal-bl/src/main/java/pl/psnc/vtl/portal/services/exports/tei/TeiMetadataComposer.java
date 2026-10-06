package pl.psnc.vtl.portal.services.exports.tei;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.transcriptions.Actor;
import pl.psnc.vtl.portal.model.transcriptions.AnalyticInfo;
import pl.psnc.vtl.portal.model.transcriptions.MonographicInfo;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@UtilityClass
public class TeiMetadataComposer {

    public void initializeMetadata(Document document, String idno, String repositiory) {
        Element sourceDesc = (Element) document.selectSingleNode("//sourceDesc");
        Element msIdentifier = sourceDesc.addElement("msDesc").addElement("msIdentifier");
        msIdentifier.addElement("repositiory").setText(repositiory);
        msIdentifier.addElement("idno").setText(idno);
    }

    public void addMetadataFromPage(Document document, Page page) {
        String idno = "VTL-T" + page.getTranscription().getId() + "-P" + page.getPageNo();
        Element teiHeader = (Element) document.selectSingleNode("//teiHeader");
        teiHeader.addElement("fileDesc").addElement("sourceDesc");
        initializeMetadata(document, idno, "VTL-REPO");
    }

    public void addMetadataFromTranscription(Document document, Transcription transcription) {
        String idno = "VTL-T" + transcription.getId();
        Element teiHeader = (Element) document.selectSingleNode("//teiHeader");
        if (transcription.isToSingleFileExport()){
            try (InputStream xmlInputStream = new ByteArrayInputStream(transcription.getTeiHeader().getBytes())) {
                SAXReader reader = new SAXReader();
                Document tmpHeaderDoc = reader.read(xmlInputStream);
                teiHeader.setContent(tmpHeaderDoc.getRootElement().content());
            } catch (IOException | DocumentException e) {
                log.error("Unable to include teiHeader for exported transcription (id = {})", transcription.getId());
            }
        }
        else {
            Element fileDesc = teiHeader.addElement("fileDesc");
            Element teiBiblStruct = fileDesc.addElement("sourceDesc")
                    .addElement("biblStruct");

            //fill teiHeader.fileDesc.sourceDesc.biblStruct.analytic
            // and teiHeader.fileDesc.sourceDesc.biblStruct.monogr
            initializeAnalytic(fileDesc, teiBiblStruct, transcription);
            initializeMonographic(fileDesc, teiBiblStruct, transcription);

            //fill teiHeader.profileDesc
            initializeProfileDesc(teiHeader.addElement("profileDesc"), transcription);
            initializeMetadata(document, idno, "VTL-REPO");
        }
    }

    private void putActor(Actor actor, Element teiAnalytic){
        Element teiActor = null;
        //if role is empty or different than author we have editor
        if (actor.getRole() != null && !"author".equalsIgnoreCase(actor.getRole().getCode())) {
            teiActor = teiAnalytic.addElement("editor");
            if (actor.getRole() != null){
                teiActor.addAttribute("role", actor.getRole().getCode());
            }
        }
        else
            teiActor = teiAnalytic.addElement("author");
        teiActor.addAttribute("id", actor.getId().toString());
        teiActor.addElement("persName").setText(actor.getName());


    }

    private Element initializeAnalytic(Element fileDesc, Element teiBiblStruct, Transcription transcription){
        Element teiAnalytic = teiBiblStruct.addElement("analytic");
        AnalyticInfo analyticInfo = transcription.getAnalyticInfo();
        if (analyticInfo ==null)
            return  teiAnalytic;

        //set title and language
        if (!StringUtils.isEmpty(analyticInfo.getTitle())) {
            teiAnalytic.addElement("title").setText(analyticInfo.getTitle());
            fileDesc.addElement("titleStmt").addElement("title")
                    .addAttribute("level", "a").setText(analyticInfo.getTitle());
        }
        if (analyticInfo.getLanguage() != null){
            teiAnalytic.addElement("textLang").addAttribute("mainLang", analyticInfo.getLanguage().getCode());
        }

        //set all actors
        analyticInfo.getActors().forEach(actor -> putActor(actor, teiAnalytic));

        return  teiAnalytic;

    }

    private Element initializeMonographic(Element fileDesc, Element teiBiblStruct, Transcription transcription){
        Element teiMonographic = teiBiblStruct.addElement("monogr");
        MonographicInfo monographicInfo = transcription.getMonographicInfo();
        if (monographicInfo == null)
            return  teiMonographic;

        //set title and language
        if (!StringUtils.isEmpty(monographicInfo.getTitle())) {
            teiMonographic.addElement("title").setText(monographicInfo.getTitle());
            fileDesc.addElement("titleStmt").addElement("title")
                    .addAttribute("level", "m").setText(monographicInfo.getTitle());
        }
        if (monographicInfo.getLanguage() != null){
            teiMonographic.addElement("textLang").addAttribute("mainLang", monographicInfo.getLanguage().getCode());
        }

        //set all actors
        monographicInfo.getActors().forEach(actor -> putActor(actor, teiMonographic));

        Element imprint = teiBiblStruct.addElement("imprint");

        //set chapter/volume/issue
        if (monographicInfo.getVolumeNo() != null){
            Element biblScope = teiBiblStruct.addElement("biblScope");
            biblScope.addAttribute("unit", "volume");
            biblScope.setText(monographicInfo.getVolumeNo().toString());
        }
        if (monographicInfo.getChapterNo() != null){
            Element biblScope = teiBiblStruct.addElement("biblScope");
            biblScope.addAttribute("unit", "chapter");
            biblScope.setText(monographicInfo.getChapterNo().toString());
        }
        if (monographicInfo.getIssueNo() != null){
            Element biblScope = teiBiblStruct.addElement("biblScope");
            biblScope.addAttribute("unit", "issue");
            biblScope.setText(monographicInfo.getIssueNo().toString());
        }

        //set page or pages range
        if (monographicInfo.getPageRange() != null){
            Element biblScope = teiBiblStruct.addElement("biblScope");
            biblScope.addAttribute("unit", "page");
            if (monographicInfo.getPageRange().contains("-")) {
                String pageFrom = monographicInfo.getPageRange().split("-")[0];
                String pageTo = monographicInfo.getPageRange().split("-")[1];
                biblScope.addAttribute("from", pageFrom);
                biblScope.addAttribute("to", pageTo);
            } else {
                biblScope.setText(monographicInfo.getPageRange());
            }
        }

        //set publisher/publication place and date
        if (monographicInfo.getPublisher() != null){
            imprint.addElement("publisher").setText(monographicInfo.getPublisher());
        }
        if (monographicInfo.getPublicationPlace() != null){
            imprint.addElement("pubPlace").setText(monographicInfo.getPublicationPlace());
        }
        if (monographicInfo.getPublicationDate() != null){
            imprint.addElement("date").setText(monographicInfo.getPublicationDate());
        }

        //set idno with isbn/eisbn/issn/eissn (optional)
        if (monographicInfo.getIsbn() != null) {
            teiMonographic.addElement("idno").addAttribute("type", "isbn").setText(monographicInfo.getIsbn());
        }
        if (monographicInfo.getIssn() != null) {
            teiMonographic.addElement("idno").addAttribute("type", "issn").setText(monographicInfo.getIssn());
        }
        if (monographicInfo.getEisbn() != null) {
            teiMonographic.addElement("idno").addAttribute("type", "eisbn").setText(monographicInfo.getEisbn());
        }
        if (monographicInfo.getEissn() != null) {
            teiMonographic.addElement("idno").addAttribute("type", "eissn").setText(monographicInfo.getEisbn());
        }

        return  teiMonographic;
    }

    private Element initializeProfileDesc(Element teiProfileDesc, Transcription transcription){
        if (transcription.getProfileDesc() == null)
            return teiProfileDesc;

        if (transcription.getProfileDesc().getKeywords() != null) {
            Element teiKeywords = teiProfileDesc.addElement("textClass").addElement("keywords");
            transcription.getProfileDesc().getKeywords().forEach( k -> teiKeywords.addElement("term").setText(k));
        }

        if (transcription.getProfileDesc().getSummary() != null) {
            teiProfileDesc.addElement("abstract").setText(transcription.getProfileDesc().getSummary());
        }

        return teiProfileDesc;
    }
}
