package pl.psnc.vtl.portal.model.verses;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * List form verse annotations.
 */
public enum ListForm {

    END_OF_ITEM,
    END_OF_LIST;

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }


    @Override
    public String toString() {
        return getValue();
    }

}
