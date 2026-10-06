package pl.psnc.vtl.portal.dto.transcriptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;

/**
 * Transcription with search hit count.
 */
@Data
@AllArgsConstructor
public class TranscriptionWithHitCount {

    /**
     * Transcription reference.
     */
    private Transcription transcription;

    //TODO: is this field set anywhere?
    /**
     * Search hit count.
     */
    private Long hitCount;
}
