package pl.psnc.vtl.portal.dto.transcriptions;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Transcription user core object.
 */
@Data
@NoArgsConstructor
public class TranscriptionUserCore {

    /**
     * User id.
     */
    private Long userId;

    /**
     * User role code.
     */
    private String roleCode;

}
