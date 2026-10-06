package pl.psnc.vtl.portal.services.parsers.tei.converters;

import pl.psnc.vtl.portal.model.verses.Formatting;
import pl.psnc.vtl.portal.model.verses.FormeWork;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;
import pl.psnc.vtl.portal.services.parsers.ocr.converters.LineConverter;

import java.util.List;

public class TeiFooterConverter extends LineConverter {
    public TeiFooterConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    @Override
    protected void enrichResult(List<Verse> resultVerses) {
        addEndOfFooterMarker(resultVerses);
    }

    private void addEndOfFooterMarker(List<Verse> resultVerses) {
        if(!resultVerses.isEmpty()){
            Formatting formatting = new Formatting();
            formatting.setFormeWork(FormeWork.FOOTER);
            resultVerses.get(resultVerses.size() - 1).setFormatting(formatting);
        }
    }
}
