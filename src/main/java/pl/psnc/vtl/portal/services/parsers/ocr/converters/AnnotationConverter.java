package pl.psnc.vtl.portal.services.parsers.ocr.converters;

import pl.psnc.vtl.portal.model.verses.Formatting;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;

import java.util.List;

public class AnnotationConverter extends LineConverter {

    public AnnotationConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    @Override
    protected void enrichResult(List<Verse> resultVerses) {
        addEndOfAnnotationMarker(resultVerses);
    }

    private void addEndOfAnnotationMarker(List<Verse> resultVerses) {
        if(!resultVerses.isEmpty()){
            Formatting formatting = new Formatting();
            formatting.setAnnotation(true);
            resultVerses.get(resultVerses.size() - 1).setFormatting(formatting);
        }
    }
}
