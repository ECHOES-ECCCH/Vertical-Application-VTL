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

public class TeiTableConverter extends Converter {
    public TeiTableConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    @Override
    public List<Verse> convert(Element element) {
        Elements rows = element.getElementsByAttributeValue("type", Tei.TEI_ROW_TYPE_NAME);
        ArrayList<Verse> results = new ArrayList<>();
        rows.forEach(row -> results.addAll(converterFactory.getConverter(row).convert(row)));
        addEndOfTableMarker(results);
        return results;
    }

    private void addEndOfTableMarker(List<Verse> resultVerses) {
        if (!resultVerses.isEmpty()) {
            Formatting formatting = new Formatting();
            formatting.setTabularForm(TabularForm.END_OF_TABLE);
            resultVerses.get(resultVerses.size() - 1).setFormatting(formatting);
        }
    }
}
