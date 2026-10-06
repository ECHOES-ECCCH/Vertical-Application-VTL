package pl.psnc.vtl.portal.services.parsers.tei.converters;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.psnc.vtl.portal.model.verses.Formatting;
import pl.psnc.vtl.portal.model.verses.TabularForm;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.Converter;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;
import pl.psnc.vtl.portal.services.parsers.tei.Tei;

import java.util.ArrayList;
import java.util.List;

public class TeiRowConverter extends Converter {

    public TeiRowConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    @Override
    public List<Verse> convert(Element element) {
        Elements cells = element.getElementsByAttributeValue("type", Tei.TEI_CELL_TYPE_NAME);
        ArrayList<Verse> results = new ArrayList<>();
        cells.forEach(cell -> results.addAll(converterFactory.getConverter(cell).convert(cell)));
        addEndOfRowMarker(results);
        return results;
    }

    private void addEndOfRowMarker(List<Verse> resultVerses) {
        if (!resultVerses.isEmpty()) {
            Formatting formatting = new Formatting();
            formatting.setTabularForm(TabularForm.END_OF_ROW);
            resultVerses.get(resultVerses.size() - 1).setFormatting(formatting);
        }
    }
}
