package pl.psnc.vtl.portal.services.eBook;

import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.core.convert.converter.Converter;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;

public enum Ebook implements Converter<String, TranscriptionPageContentType> {
    MOBI,
    PDF,
    EPUB;

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }

    @Override
    public TranscriptionPageContentType convert(String value) {
        return TranscriptionPageContentType.valueOf(value.toUpperCase());
    }
}
