package pl.psnc.vtl.portal.dto.transcriptions;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Transcription continuation object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranscriptionContinuation {

    /**
     * Last page id.
     */
    private Long lastPageId;

    /**
     * State of work on last page.
     */
    private State state;


    public enum State {

        NOT_STARTED,
        IN_PROGRESS,
        FINISHED;

        @JsonValue
        public String getValue() {
            return name().replace('_', '-').toLowerCase();
        }

        @Override
        public String toString() {
            return getValue();
        }

    }

}
