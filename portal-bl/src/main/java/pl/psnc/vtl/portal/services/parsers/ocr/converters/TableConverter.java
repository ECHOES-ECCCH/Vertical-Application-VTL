package pl.psnc.vtl.portal.services.parsers.ocr.converters;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.psnc.vtl.portal.model.verses.Formatting;
import pl.psnc.vtl.portal.model.verses.TabularForm;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.Converter;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;
import pl.psnc.vtl.portal.services.parsers.ocr.Hocr;

import java.util.ArrayList;
import java.util.List;

public class TableConverter extends Converter {

    public TableConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    @Override
    public List<Verse> convert(Element element) {
        List<Verse> result = new ArrayList<>();

        Elements tableRows = element.getElementsByTag("tr");
        tableRows.forEach(tableRow -> {
            Elements rowColumns = tableRow.getElementsByTag("td");
            rowColumns.forEach(rowColumn -> {
                Elements lines = rowColumn.getElementsByAttributeValue("class", Hocr.OCR_LINE_CLASS_NAME);
                lines.forEach(line -> {
                    Verse verseWithContentAndBboxOnly = elementsSupplier.createVerseWithContentAndBboxOnly(line);
                    result.add(verseWithContentAndBboxOnly);
                });
                markAsEndOfCell(result);
            });
            markAsEndOfRow(result);
        });
        markAsEndOfTable(result);
        return result;
    }

    private void markAsEndOfCell(List<Verse> verses) {
        if(!verses.isEmpty()){
            Formatting f = new Formatting();
            f.setTabularForm(TabularForm.END_OF_CELL);
            verses.get(verses.size() - 1).setFormatting(f);
        }
    }

    private void markAsEndOfRow(List<Verse> result) {
        if(!result.isEmpty()) {
            Formatting f = new Formatting();
            f.setTabularForm(TabularForm.END_OF_ROW);
            result.get(result.size() - 1).setFormatting(f);
        }
    }

    private void markAsEndOfTable(List<Verse> result){
        if(!result.isEmpty()){
            Formatting f = new Formatting();
            f.setTabularForm(TabularForm.END_OF_TABLE);
            result.get(result.size() - 1).setFormatting(f);
        }
    }
}
