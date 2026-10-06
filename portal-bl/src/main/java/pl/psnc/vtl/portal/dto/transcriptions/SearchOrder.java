package pl.psnc.vtl.portal.dto.transcriptions;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.springframework.data.domain.Sort;

/**
 * Search order object.
 */
public enum SearchOrder {

    /**
     * Sort transcriptions by last modification date.
     */
    LAST_MODIFICATION_DATE(Sort.Direction.DESC, "lastModificationDate", true),

    /**
     * Sort transcriptions by creation date.
     */
    DATE_CREATED(Sort.Direction.DESC, "dateCreated", true),

    /**
     * Sort transcriptions by title.
     */
    TITLE(Sort.Direction.ASC, "titleLabel", true),

    /**
     * Sort transcription by result hit count.
     */
    HIT_COUNT(Sort.Direction.DESC, "hitCount", false),

    /**
     * Sort transcriptions by percent of transcribed verses.
     */
    PERCENT_OF_TRANSCRIBED(Sort.Direction.DESC, "percentOfTranscribed", true),

    /**
     * Sort transcriptions by creator email.
     */
    EMAIL(Sort.Direction.ASC, "creatoremail", false);

    /**
     * Sort direction.
     */
    private final Sort.Direction defaultDirection;

    /**
     * Sort field.
     */
    @Getter
    private final String field;

    /**
     * Is field from transcription or is it calculated.
     */
    @Getter
    private final boolean isTranscriptionField;

    SearchOrder(Sort.Direction defaultDirection, String field, boolean isTranscriptionField) {
        this.defaultDirection = defaultDirection;
        this.field = field;
        this.isTranscriptionField = isTranscriptionField;
    }

    public Sort toSort(Sort.Direction sortDirection) {
        return Sort.by(sortDirection == null ? defaultDirection : sortDirection, field);
    }

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }

}
