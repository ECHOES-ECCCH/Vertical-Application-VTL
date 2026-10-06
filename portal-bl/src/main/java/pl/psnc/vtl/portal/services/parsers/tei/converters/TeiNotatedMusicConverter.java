package pl.psnc.vtl.portal.services.parsers.tei.converters;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.Converter;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;

import java.util.ArrayList;
import java.util.List;

public class TeiNotatedMusicConverter extends Converter {
    public TeiNotatedMusicConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    @Override
    public List<Verse> convert(Element element) {
        List<Verse> result = new ArrayList<>();
        Elements lines = element.getElementsByTag("notatedMusic");
        lines.forEach(line -> result.add(elementsSupplier.createVerseWithContentAndBboxOnly(line)));
        addNotatedMusic(result);
        return result;
    }

    private void addNotatedMusic(List<Verse> result) {
        if (!result.isEmpty()) {
            result.get(result.size() - 1).getFormatting().setNotatedMusic(true);
        }
    }
}
