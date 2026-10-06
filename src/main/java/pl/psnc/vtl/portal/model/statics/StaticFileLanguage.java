package pl.psnc.vtl.portal.model.statics;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Enum containing language codes.
 */
@RequiredArgsConstructor
public enum StaticFileLanguage {
    PL("pl"), EN("en"), DE("de");

    @Getter
    private final String name;

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }


    @Override
    public String toString() {
        return getValue();
    }

    public static StaticFileLanguage ofOrDefault(String name) {
        return Arrays
                .stream(StaticFileLanguage.values())
                .filter(c -> c.name.equalsIgnoreCase(name))
                .findFirst()
                .orElse(StaticFileLanguage.PL);
    }
}
