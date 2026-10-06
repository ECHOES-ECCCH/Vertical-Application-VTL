package pl.psnc.vtl.portal.services.parsers.ocr.converters;

import pl.psnc.vtl.portal.model.verses.Formatting;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;

import java.util.List;

public class MarginaliaConverter extends LineConverter {

    public MarginaliaConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    @Override
    protected void enrichResult(List<Verse> resultVerses) {
        addEndOfMarginaliaMarker(resultVerses);
    }

    private void addEndOfMarginaliaMarker(List<Verse> resultVerses) {
        if(!resultVerses.isEmpty()){
            Formatting formatting = new Formatting();
            formatting.setMarginalia(true);
            resultVerses.get(resultVerses.size() - 1).setFormatting(formatting);
        }
    }
}
