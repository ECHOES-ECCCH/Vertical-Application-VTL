package pl.psnc.vtl.portal.dto.transcriptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Transcription export accept info object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionExportAcceptInfo {

    /**
     * Is transcription accepted or rejected to export.
     */
    Boolean accepted;

    /**
     * Message that will be sent to transcription owner if export request was rejected.
     */
    String message;
}
