package pl.psnc.vtl.portal.model.verses;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Status of the verse
 */
public enum VerseStatus {

    /**
     * Verse without status.
     */
    NONE,

    /**
     * Verse marked as transcribed.
     */
    TRANSCRIBED,

    /**
     * Verse marked as incorrect.
     */
    INCORRECT,

    /**
     * Verse marked as verified.
     */
    VERIFIED;

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }

}
