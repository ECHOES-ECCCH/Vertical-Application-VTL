package pl.psnc.vtl.portal.model.transcriptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Helper class for setting transcription visibility.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Hidden {

    /**
     * Transcription visibility.
     */
    Boolean hidden;
}
