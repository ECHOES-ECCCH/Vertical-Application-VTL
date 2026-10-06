package pl.psnc.vtl.portal.controllers.pages;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.convert.converter.Converter;

import java.util.Arrays;
import java.util.List;


@AllArgsConstructor
@Getter
public enum TranscriptionPageContentType implements Converter<String, TranscriptionPageContentType> {


    PLAIN_TEXT(Arrays.asList("txt"), "text/plain; charset=utf-8"),
    HOCR(Arrays.asList("hocr", "html"), "text/html; charset=utf-8"),
    ALTO(Arrays.asList("alto.xml"), "application/alto+xml; charset=utf-8"),
    TEI(Arrays.asList("tei", "xml"), "application/xml; charset=utf-8");

    private List<String> extensions;
    private String contentType;

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
