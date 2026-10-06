package pl.psnc.vtl.portal.dto.permissions;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * View mode enum.
 */
public enum ViewMode {

    /**
     * User have read only access to transcription.
     */
    READ_ONLY,

    /**
     * User have transcribe access to transcription.
     */
    TRANSCRIBE,

    /**
     * User have verify access to transcription.
     */
    VERIFY,

    /**
     * User have admin access to transcription.
     */
    ADMIN,

    /**
     * User have student access to transcription.
     */
    STUDENT;

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }

}
