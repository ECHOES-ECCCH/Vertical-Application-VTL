package pl.psnc.vtl.portal.services.parsers.ocr.converters;

import pl.psnc.vtl.portal.model.verses.Formatting;
import pl.psnc.vtl.portal.model.verses.FormeWork;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;

import java.util.List;

public class HeaderConverter extends LineConverter {

    public HeaderConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    @Override
    protected void enrichResult(List<Verse> resultVerses) {
        addEndOfHeaderMarker(resultVerses);
    }

    private void addEndOfHeaderMarker(List<Verse> resultVerses) {
        if(!resultVerses.isEmpty()){
            Formatting formatting = new Formatting();
            formatting.setFormeWork(FormeWork.HEADER);
            resultVerses.get(resultVerses.size() - 1).setFormatting(formatting);
        }
    }
}
