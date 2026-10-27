package pl.psnc.vtl.portal.services.parsers;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Element;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.services.parsers.ocr.HocrElementsSupplier;
import pl.psnc.vtl.portal.services.parsers.tei.TeiElementsSupplier;
import pl.psnc.vtl.portal.services.parsers.utils.VerseNumberSupplier;

@Getter
public abstract class ConverterFactory {

    protected final ConverterSettings converterSettings;

    protected ConverterFactory(TranscriptionPageContentType transcriptionPageContentType) {
        this.converterSettings = ConverterSettings.getConversionSettings(transcriptionPageContentType);
        this.converterSettings.setConverterFactory(this);
    }


    public abstract Converter getConverter(Element element);

    public void resetVerseNumbering() {
        this.converterSettings.verseNumberSupplier.reset();
    }

    @Data
    @RequiredArgsConstructor
    public static class ConverterSettings {
        private final VerseNumberSupplier verseNumberSupplier;
        private final DocumentElementsSupplier elementsSupplier;
        private ConverterFactory converterFactory;

        private static ConverterSettings getConversionSettings(TranscriptionPageContentType contentType) throws UnsupportedOperationException {
            switch (contentType) {
                case HOCR:
                    return new ConverterSettings(new VerseNumberSupplier(), new HocrElementsSupplier());
                case TEI:
                    return new ConverterSettings(new VerseNumberSupplier(), new TeiElementsSupplier());
                case PLAIN_TEXT:
                    return new ConverterSettings(new VerseNumberSupplier(), null);
            }
            throw new UnsupportedOperationException(String.format("Page content parser for content-type: {1} not exist. Check supported types.", contentType));
        }
    }
}
