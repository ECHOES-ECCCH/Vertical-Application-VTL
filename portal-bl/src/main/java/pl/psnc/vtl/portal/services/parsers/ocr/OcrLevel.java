package pl.psnc.vtl.portal.services.parsers.ocr;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OcrLevel {
    VERSE, WORD;

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }
}
