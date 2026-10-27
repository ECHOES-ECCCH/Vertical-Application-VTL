package pl.psnc.vtl.portal.services.parsers.tei.converters;

import pl.psnc.vtl.portal.model.verses.Formatting;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;

import java.util.List;

public class TeiCaptionConverter extends TeiLineConverter {
    public TeiCaptionConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    @Override
    protected void enrichResult(List<Verse> resultVerses) {
        addEndOfCaptionMarker(resultVerses);
    }

    private void addEndOfCaptionMarker(List<Verse> resultVerses) {
        if(!resultVerses.isEmpty()){
            Formatting formatting = new Formatting();
            formatting.setImageCaption(true);
            resultVerses.forEach(v -> v.setFormatting(formatting));
        }
    }
}
