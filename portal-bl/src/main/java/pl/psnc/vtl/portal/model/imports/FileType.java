package pl.psnc.vtl.portal.model.imports;

import com.fasterxml.jackson.annotation.JsonValue;
import pl.psnc.vtl.portal.model.transcriptions.CreationMode;

/**
 * Enum containing information about import file format.
 */
public enum FileType {

    METS,

    IIIF,

    TEI,

    DACE;

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }

    public static FileType from(CreationMode creationMode) {
        switch (creationMode) {
        case METS:
            return METS;
        case IIIF:
            return IIIF;
        case TEI:
            return TEI;
        case DACE:
            return DACE;
        default:
            throw new IllegalArgumentException("Unsupported creation mode: " + creationMode);
        }
    }
}
