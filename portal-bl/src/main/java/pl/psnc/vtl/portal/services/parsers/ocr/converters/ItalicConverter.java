package pl.psnc.vtl.portal.services.parsers.ocr.converters;

import pl.psnc.vtl.portal.model.verses.Formatting;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;

import java.util.List;

public class ItalicConverter extends LineConverter{

    public ItalicConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    @Override
    protected void enrichResult(List<Verse> resultVerses) {
        addEndOfItalicMarker(resultVerses);
    }

    private void addEndOfItalicMarker(List<Verse> resultVerses) {
        if(!resultVerses.isEmpty()){
            Formatting formatting = new Formatting();
            formatting.setItalic(true);
            for(Verse v: resultVerses) {
                v.setFormatting(formatting);
            }
        }
    }
}