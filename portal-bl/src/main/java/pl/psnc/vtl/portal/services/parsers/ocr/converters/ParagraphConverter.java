package pl.psnc.vtl.portal.services.parsers.ocr.converters;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.Converter;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;

import java.util.ArrayList;
import java.util.List;

public class ParagraphConverter extends Converter {

    public ParagraphConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    @Override
    public List<Verse> convert(Element element) {
        List<Verse> result = new ArrayList<>();
        Elements lines = elementsSupplier.supplyLines(element);
        lines.forEach(line -> result.add(elementsSupplier.createVerseWithContentAndBboxOnly(line)));
        addEndOfParagraph(result);
        return result;
    }

    private void addEndOfParagraph(List<Verse> result) {
        if (!result.isEmpty()) {
            result.get(result.size() - 1).getFormatting().setEndOfParagraph(true);
        }
    }
}
