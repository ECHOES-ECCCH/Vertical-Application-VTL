package pl.psnc.vtl.portal.dto.transcriptions;

import lombok.Data;

/**
 * Transcription metadata core object.
 */
@Data
public class TranscriptionMetadataCore {

    public TranscriptionMetadataCore() {
        this.monographicInfo = new MonographicInfoCore();
        this.transcriptionInfo = new TranscriptionInfoCore();
        this.profileDesc = new ProfileDescCore();
        this.analyticInfo = new AnalyticInfoCore();
    }

    public TranscriptionMetadataCore(TranscriptionCore transcriptionCore) {
        this.monographicInfo = transcriptionCore.getMonographicInfo();
        this.transcriptionInfo = transcriptionCore.getTranscriptionInfo();
        this.profileDesc = transcriptionCore.getProfileDesc();
        this.analyticInfo = transcriptionCore.getAnalyticInfo();
    }

    /**
     * Analytic metadata.
     */
    private AnalyticInfoCore analyticInfo;

    /**
     * Profile description metadata.
     */
    private ProfileDescCore profileDesc;

    /**
     * Monographic metadata.
     */
    private MonographicInfoCore monographicInfo;

    /**
     * Transcription metadata.
     */
    private TranscriptionInfoCore transcriptionInfo;


}