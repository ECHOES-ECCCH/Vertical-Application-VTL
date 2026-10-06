package pl.psnc.vtl.portal.services.parsers.tei.converters;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.Converter;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class TeiLineConverter extends Converter {

    protected TeiLineConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    @Override
    public List<Verse> convert(Element element) {
        List<Verse> result = new ArrayList<>();
        Elements lines = elementsSupplier.supplyLines(element);
        lines.forEach(line -> {
            Verse verseWithContentAndBboxOnly = elementsSupplier.createVerseWithContentAndBboxOnly(line);
            result.add(verseWithContentAndBboxOnly);
        });
        enrichResult(result);
        return result;
    }

    protected abstract void enrichResult(List<Verse> resultVerses);
}
