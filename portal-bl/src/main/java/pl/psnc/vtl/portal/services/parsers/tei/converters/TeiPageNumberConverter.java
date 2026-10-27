package pl.psnc.vtl.portal.services.parsers.tei.converters;

import pl.psnc.vtl.portal.model.verses.Formatting;
import pl.psnc.vtl.portal.model.verses.FormeWork;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;

import java.util.List;

public class TeiPageNumberConverter extends TeiLineConverter {
    public TeiPageNumberConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    @Override
    protected void enrichResult(List<Verse> resultVerses) {
        addPageNumberMarker(resultVerses);
    }

    private void addPageNumberMarker(List<Verse> resultVerses) {
        if(!resultVerses.isEmpty()){
            Formatting formatting = new Formatting();
            formatting.setFormeWork(FormeWork.PAGE_NO);
            resultVerses.get(resultVerses.size() - 1).setFormatting(formatting);
        }
    }
}
