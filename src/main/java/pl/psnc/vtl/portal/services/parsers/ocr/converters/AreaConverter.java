package pl.psnc.vtl.portal.services.parsers.ocr.converters;

import org.jsoup.nodes.Element;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.Converter;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;

import java.util.ArrayList;
import java.util.List;

public class AreaConverter extends Converter {


    public AreaConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    @Override
    public List<Verse> convert(Element element) {

        List<Verse> result = new ArrayList<>();
        element.children().forEach(childElement -> {
            Converter converter = converterFactory.getConverter(childElement);
            result.addAll(converter.convert(childElement));
        });
        return result;
    }
}
