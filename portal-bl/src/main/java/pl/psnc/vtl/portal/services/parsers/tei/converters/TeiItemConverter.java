package pl.psnc.vtl.portal.services.parsers.tei.converters;

import pl.psnc.vtl.portal.model.verses.*;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;

import java.util.List;

public class TeiItemConverter extends TeiLineConverter {

    public TeiItemConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    @Override
    protected void enrichResult(List<Verse> resultVerses) {
        addEndOfHeadingMarker(resultVerses);
    }

    private void addEndOfHeadingMarker(List<Verse> resultVerses) {
        if (!resultVerses.isEmpty()) {
            Formatting formatting = new Formatting();
            formatting.setListForm(ListForm.END_OF_ITEM);
            resultVerses.get(resultVerses.size() - 1).setFormatting(formatting);
        }
    }
}

