package pl.psnc.vtl.portal.services.parsers;

import org.jsoup.nodes.Element;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.utils.VerseNumberSupplier;

import java.util.List;

public abstract class Converter {

    protected final ConverterFactory converterFactory;
    protected final DocumentElementsSupplier elementsSupplier;

    protected Converter(ConverterFactory.ConverterSettings converterSettings) {
        this.converterFactory = converterSettings.getConverterFactory();
        this.elementsSupplier = converterSettings.getElementsSupplier();
        this.elementsSupplier.verseNumberSupplier = getVerseNumberSupplier();
    }

    private VerseNumberSupplier getVerseNumberSupplier(){
        return this.converterFactory.getConverterSettings().getVerseNumberSupplier();
    }

    public abstract List<Verse> convert(Element element);

}
