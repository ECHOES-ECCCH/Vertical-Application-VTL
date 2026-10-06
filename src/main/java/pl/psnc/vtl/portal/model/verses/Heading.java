package pl.psnc.vtl.portal.model.verses;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Heading verse annotations.
 */
public enum Heading {

    END_OF_HEADING_1,
    END_OF_HEADING_2,
    END_OF_HEADING_3;

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }


    @Override
    public String toString() {
        return getValue();
    }
}
