package pl.psnc.vtl.portal.model.indexes;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

/**
 * Index type enum.
 */
public enum IndexType {

    PERSON,
    PLACE,
    TERM;

    @JsonValue
    public String getValue(){
        return name().replace('_', '-').toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString(){
        return getValue();
    }

}
