package pl.psnc.vtl.portal.model.permissions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Transcription - Group connection
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionGroupId implements Serializable {

    /**
     * Transcription id.
     */
    private Long transcription;

    /**
     * Group id.
     */
    private Long group;

}
