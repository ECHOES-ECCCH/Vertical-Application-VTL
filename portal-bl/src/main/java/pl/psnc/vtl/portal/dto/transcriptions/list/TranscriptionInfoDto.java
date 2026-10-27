package pl.psnc.vtl.portal.dto.transcriptions.list;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Transcription info dto object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class TranscriptionInfoDto {

    /**
     * Transcription id.
     */
    protected long id;

    /**
     * Analytic info.
     */
    protected AnalyticInfoDto analyticInfo;
}
