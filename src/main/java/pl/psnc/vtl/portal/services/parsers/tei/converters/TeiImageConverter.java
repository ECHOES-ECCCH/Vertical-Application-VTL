package pl.psnc.vtl.portal.services.parsers.tei.converters;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.psnc.vtl.portal.model.verses.Formatting;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.Converter;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;
import pl.psnc.vtl.portal.services.parsers.tei.Tei;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TeiImageConverter extends Converter {

    public TeiImageConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    private boolean isImageBeforeCaptions(String elementHtml){
        int graphicIdx = elementHtml.indexOf("<graphic url=");
        int captionIndex = elementHtml.indexOf("type=\"figureHead\">");
        return graphicIdx < captionIndex;
    }

    @Override
    public List<Verse> convert(Element element) {
        ArrayList<Verse> results = new ArrayList<>();

        if(isImageBeforeCaptions(element.html())){
            generateImage(element, results);
            generateCaptions(element, results);
        }
        else{
            generateCaptions(element, results);
            generateImage(element, results);
        }

        return results;
    }


    private void generateImage(Element element, ArrayList<Verse> results) {
        if(element.html().contains("<graphic url=")) {
            Verse verse = elementsSupplier.createVerseWithContentAndBboxOnly(element);
            verse.setImage(true);

            //try parse image data
            try {
                Element graphic = element.getElementsByAttributeValue("type", Tei.TEI_GRAPHIC_TYPE_NAME).get(0);
                verse.setBoundingBox(elementsSupplier.prepareBoundingBoxForElement(graphic));
                verse.setImage(true);
            } catch (Exception ex) {
                log.error("Cannot process TEI image data! See: " + ex.getMessage());
            }

            //TODO: here we should add image (as content) and its formatting - in the future when we add images processing
            Formatting formatting = new Formatting();
            verse.setFormatting(formatting);
            results.add(verse);
        }
    }


    private void generateCaptions(Element element, ArrayList<Verse> results) {
        Elements captions = element.getElementsByAttributeValue("type", Tei.TEI_CAPTION_TYPE_NAME);
        captions.forEach(caption -> results.addAll(converterFactory.getConverter(caption).convert(caption)));
    }
}
