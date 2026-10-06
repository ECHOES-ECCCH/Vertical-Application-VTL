package pl.psnc.vtl.portal.model.transcriptions;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Creation mode of the transcription.
 */
public enum CreationMode {

    /**
     * Transcription created using web form.
     */
    WEB_FORM,
    /**
     * Transcription imported from METS file.
     */
    METS,
    /**
     * Transcription imported from IIIF file.
     */
    IIIF,
    /**
     * Transcription imported from TEI file.
     */
    TEI,
    /**
     * Transcription imported from DACE file.
     */
    DACE;

    /**
     * @return enum name in lower case, with '_' replaced by '-'.
     */
    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }

}
