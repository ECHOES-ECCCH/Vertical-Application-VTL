package pl.psnc.vtl.portal.model.verses;

import com.fasterxml.jackson.annotation.JsonValue;


/**
 * Forme work of the verse.
 * Check https://www.tei-c.org/release/doc/tei-p5-doc/en/html/ref-fw.html for reference.
 */
public enum FormeWork {
    HEADER,
    FOOTER,
    PAGE_NO;

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }


    @Override
    public String toString() {
        return getValue();
    }
}
