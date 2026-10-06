package pl.psnc.vtl.portal.dto.transcriptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Transcription summarize object.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptionSummarize {

    /**
     * User role code.
     */
    private  String userRoleCode;

    /**
     * Is user owner of this transcription.
     */
    private Boolean isOwner;

    /**
     * User email.
     */
    private  String email;

    /**
     * User id.
     */
    private  long  userId;

    /**
     * Transcription id.
     */
    private  long  transcriptionId;

    /**
     * Number of accepted verses.
     */
    private long accepted;

    /**
     * Distance
     */
    private long distance;

    /**
     * Number of initial verse content updates.
     */
    private long filled;

    /**
     * Number of verse updates.
     */
    private long corrected;

    /**
     * Number of user rejected verses.
     */
    private long userRejected;

    /**
     * Number of rejected verses.
     */
    private long rejected;


}
