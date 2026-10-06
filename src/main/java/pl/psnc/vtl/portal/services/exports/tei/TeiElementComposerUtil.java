package pl.psnc.vtl.portal.services.exports.tei;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.words.Word;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@UtilityClass
public class TeiElementComposerUtil {

    public Map<String, String> HTML_TO_TEI_HI_REND = Map.of(
            "strong", "bold",
            "b", "bold",
            "em", "italic",
            "i", "italic",
            "u", "underline",
            "strike", "strikethrough",
            "sub", "subscript",
            "sup", "superscript");

    private boolean isValid(Verse coordVerse){
        return coordVerse != null && coordVerse.currentContent() != null;
    }

    public Element addBoundingBoxToElement(Element element, BoundingBox boundingBox) {
        if (boundingBox == null)
            return element;
        try {
            Integer lrx = boundingBox.getLowerRightX();
            Integer lry = boundingBox.getLowerRightY();
            Integer ulx = boundingBox.getUpperLeftX();
            Integer uly = boundingBox.getUpperLeftY();

            element.addAttribute("ulx", ulx.toString());
            element.addAttribute("uly", uly.toString());
            element.addAttribute("lrx", lrx.toString());
            element.addAttribute("lry", lry.toString());
        }
        catch (Exception e){
            log.error("Unable to extract bbox for exported TEI document. See: {}", e.getMessage());
        }

        return element;
    }

    private Element createLineElement(Verse coordVerse) {
        Element line = DocumentHelper.createElement("line");
        addBoundingBoxToElement(line, coordVerse.getBoundingBox());
        if (!StringUtils.isBlank(coordVerse.currentContent())) {
            if (!coordVerse.getWords().isEmpty()) {
                for (Word word : coordVerse.getWords()) {
                    Element wordZone = DocumentHelper.createElement("zone");
                    wordZone.setText(word.getContent());
                    addBoundingBoxToElement(wordZone, word.getBoundingBox());
                    line.add(wordZone);
                }
            } else {
                line.setText(coordVerse.currentContent());
            }
        }
        return line;
    }

    public Element createLinesForContentVerses(Element baseElement, List<Verse> coordVerses) {
        Optional.ofNullable(coordVerses)
                .orElse(Collections.emptyList()).stream()
                .filter(cv -> isValid(cv))
                .forEach(cv -> baseElement.add(createLineElement(cv)));

        return baseElement;
    }
}
