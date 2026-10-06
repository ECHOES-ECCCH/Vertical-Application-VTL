package pl.psnc.vtl.portal.services.parsers.ocr.converters;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.Converter;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;

import java.util.Collections;
import java.util.List;

@Slf4j
public class NoOpConverter extends Converter {

    public NoOpConverter(ConverterFactory.ConverterSettings converterSettings) {
        super(converterSettings);
    }

    @Override
    public List<Verse> convert(Element element) {
        log.info("Doing nothing");
        return Collections.emptyList();
    }
}
