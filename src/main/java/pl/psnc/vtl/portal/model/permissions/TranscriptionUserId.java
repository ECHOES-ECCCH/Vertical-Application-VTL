package pl.psnc.vtl.portal.model.permissions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Transcription - USer connection.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionUserId implements Serializable {

    /**
     * Transcription id.
     */
    private Long transcription;

    /**
     * User id.
     */
    private Long user;

}
