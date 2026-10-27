package pl.psnc.vtl.portal.model.verses;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Tabular form verse annotations.
 */
public enum TabularForm {

    END_OF_CELL,
    END_OF_ROW,
    END_OF_TABLE;

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }


    @Override
    public String toString() {
        return getValue();
    }
}
