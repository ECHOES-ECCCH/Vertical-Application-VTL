package pl.psnc.vtl.portal.services.parsers.tei.converters;

import pl.psnc.vtl.portal.model.verses.Formatting;
import pl.psnc.vtl.portal.model.verses.TabularForm;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;

import java.util.List;

public class TeiCellConverter extends TeiLineConverter {
    public TeiCellConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    @Override
    protected void enrichResult(List<Verse> resultVerses) {
        addEndOfCellMarker(resultVerses);
    }

    private void addEndOfCellMarker(List<Verse> resultVerses) {
        if (!resultVerses.isEmpty()) {
            Formatting formatting = new Formatting();
            formatting.setTabularForm(TabularForm.END_OF_CELL);
            resultVerses.get(resultVerses.size() - 1).setFormatting(formatting);
        }
    }
}
