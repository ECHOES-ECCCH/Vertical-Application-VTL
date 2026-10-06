package pl.psnc.vtl.portal.services.parsers.tei.converters;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.psnc.vtl.portal.model.verses.ListForm;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.Converter;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;
import pl.psnc.vtl.portal.services.parsers.tei.Tei;

import java.util.ArrayList;
import java.util.List;

public class TeiListConverter extends Converter {
    public TeiListConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    @Override
    public List<Verse> convert(Element element) {
        ArrayList<Verse> results = new ArrayList<>();
        Elements items = element.getElementsByAttributeValue("type", Tei.TEI_ITEM_TYPE_NAME);
        items.forEach(listItem -> results.addAll(converterFactory.getConverter(listItem).convert(listItem)));
        if (!results.isEmpty()){
            results.get(results.size()-1).getFormatting().setListForm(ListForm.END_OF_LIST);
        }
        return  results;
    }
}
