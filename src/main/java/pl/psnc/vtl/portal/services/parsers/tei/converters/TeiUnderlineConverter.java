package pl.psnc.vtl.portal.services.parsers.tei.converters;

import pl.psnc.vtl.portal.model.verses.Formatting;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;
import pl.psnc.vtl.portal.services.parsers.ocr.converters.LineConverter;

import java.util.List;

public class TeiUnderlineConverter extends LineConverter {
    public TeiUnderlineConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    @Override
    protected void enrichResult(List<Verse> resultVerses) {
        addEndOfUnderline(resultVerses);
    }

    private void addEndOfUnderline(List<Verse> resultVerses) {
        if(!resultVerses.isEmpty()){
            Formatting formatting = new Formatting();
            formatting.setUnderline(true);
            resultVerses.get(resultVerses.size() - 1).setFormatting(formatting);
        }
    }
}