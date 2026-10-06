package pl.psnc.vtl.portal.dto.transcriptions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import pl.psnc.vtl.portal.model.imports.FileType;

/**
 * Transcription core object.
 */
@Data
public class TranscriptionCore {

    public TranscriptionCore() {
        this.analyticInfo = new AnalyticInfoCore();
        this.monographicInfo = new MonographicInfoCore();
        this.transcriptionInfo = new TranscriptionInfoCore();
        this.profileDesc = new ProfileDescCore();
    }

    /**
     * File type of transcription.
     */
    private FileType fileType;

    /**
     * Is transcription analytic.
     */
    @JsonIgnore
    private boolean isAnalytic;

    /**
     * Is transcription imported from dLibra for external edit.
     */
    private boolean externalEdit;

    /**
     * Url of external source.
     */
    private String externalUrl;

    /**
     * Id of external source.
     */
    private String externalId;

    private String externalMetadataUrl;

    private FileType externalMetadataType;

    /**
     * Analytic metadata.
     */
    private AnalyticInfoCore analyticInfo;

    /**
     * Monographic metadata.
     */
    private MonographicInfoCore monographicInfo;

    /**
     * Transcription metadata.
     */
    private TranscriptionInfoCore  transcriptionInfo ;

    /**
     * Profile description metadata.
     */
    private ProfileDescCore profileDesc;

    /**
     * Does user accept regulations.
     */
    private Boolean acceptedRegulations;

    /**
     * Header of tei source file.
     */
    private String teiHeader;

    /**
     * Body of tei source file.
     */
    private String teiBody;

    /**
     * External project id.
     */
    private String projectId;

}
