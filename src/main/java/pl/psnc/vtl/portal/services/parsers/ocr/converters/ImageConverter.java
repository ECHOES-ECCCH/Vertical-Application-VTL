package pl.psnc.vtl.portal.services.parsers.ocr.converters;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.psnc.vtl.portal.model.verses.Formatting;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.Converter;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;
import pl.psnc.vtl.portal.services.parsers.ocr.Hocr;

import java.util.ArrayList;
import java.util.List;

public class ImageConverter extends Converter {

    public ImageConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    @Override
    public List<Verse> convert(Element element) {
        List<Verse> result = new ArrayList<>();
        Elements lines = element.getElementsByAttributeValue("class", Hocr.OCR_IMAGE_CLASS_NAME);
        lines.forEach(line -> result.add(elementsSupplier.createVerseWithContentAndBboxOnly(line)));
        addImageMarker(result);
        return result;
    }

    private void addImageMarker(List<Verse> resultVerses) {
        if(!resultVerses.isEmpty()){
            for(Verse v: resultVerses) {
                v.setImage(true);
            }
        }
    }

}
