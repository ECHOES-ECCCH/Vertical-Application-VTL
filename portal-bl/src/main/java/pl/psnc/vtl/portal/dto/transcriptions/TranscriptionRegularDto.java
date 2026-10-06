package pl.psnc.vtl.portal.dto.transcriptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.conf.datetime.ApiDateTimeFormatter;
import pl.psnc.vtl.portal.dto.pages.PaginatedPages;
import pl.psnc.vtl.portal.dto.permissions.ViewMode;
import pl.psnc.vtl.portal.dto.users.UserRegularDto;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.transcriptions.*;
import pl.psnc.vtl.portal.services.eBook.AsyncTaskResponse;
import pl.psnc.vtl.portal.services.layers.CustomLayerListSerializer;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Transcription regular dto object.
 */
@NoArgsConstructor
@Getter
public class TranscriptionRegularDto {

    /**
     * Transcription id.
     */
    private Long id;

    /**
     * Transcription title.
     */
    private String titleLabel;

    /**
     * Transcription source authors.
     */
    private String authorsLabel;

    /**
     * Transcription creation date.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.dateTimePattern)
    private LocalDateTime dateCreated;

    /**
     * Transcription publication date.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.dateTimePattern)
    private LocalDateTime datePublished;

    /**
     * Transcription creatror.
     */
    private UserRegularDto creator;

    /**
     * Transcription last modification date.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.dateTimePattern)
    private LocalDateTime lastModificationDate;

    /**
     * Transcription creation mode.
     */
    private CreationMode creationMode;

    /**
     * Transcription export status.
     */
    private TranscriptionExportStatus exportStatus;

    /**
     * Source external url.
     */
    private String externalUrl;

    /**
     * Source external id.
     */
    private String externalId;

    /**
     * Analytic metadata.
     */
    private AnalyticInfo analyticInfo;

    /**
     * Monographic metadata.
     */
    private MonographicInfo monographicInfo;

    /**
     * Transcription metadata.
     */
    private TranscriptionInfo transcriptionInfo;

    /**
     * Profile description metadata.
     */
    private ProfileDesc profileDesc;

    /**
     * Is transcription hidden.
     * Hidden transcription is private; not public.
     */
    private boolean hidden;

    /**
     * External project id.
     */
    private String projectId;

    /**
     * Max transcription space quota.
     */
    private Integer quota;

    /**
     * Used up transcription quota.
     */
    private Integer usedSpaceQuota;

    /**
     * Percent of transcribed verses.
     */
    private Integer percentOfTranscribed;

    /**
     * Members count.
     */
    private Integer userCount;

    /**
     * List of the layers.
     */
    @JsonSerialize(using = CustomLayerListSerializer.class)
    private List<Layer> layers;

    /**
     * Paginated list of pages.
     */
    private PaginatedPages pages;

    /**
     * View mode of current user.
     */
    private ViewMode viewMode;

    /**
     * Percent of verified verses.
     */
    private Integer percentOfVerified;

    /**
     * Transcription continuation.
     */
    private TranscriptionContinuation continuation;

    /**
     * Info about async task progress.
     */
    private AsyncTaskResponse asyncProgressInfo;

    public TranscriptionRegularDto(Transcription transcription) {
        this.id = transcription.getId();
        this.titleLabel = transcription.getTitleLabel();
        this.authorsLabel = transcription.getAuthorsLabel();
        this.dateCreated = transcription.getDateCreated();
        this.datePublished = transcription.getDatePublished();
        this.creator = new UserRegularDto(transcription.getCreator());
        this.lastModificationDate = transcription.getLastModificationDate();
        this.creationMode = transcription.getCreationMode();
        this.exportStatus = transcription.getExportStatus();
        this.externalUrl = transcription.getExternalUrl();
        this.externalId = transcription.getExternalId();
        this.analyticInfo = transcription.getAnalyticInfo();
        this.monographicInfo = transcription.getMonographicInfo();
        this.transcriptionInfo = transcription.getTranscriptionInfo();
        this.profileDesc = transcription.getProfileDesc();
        this.hidden = transcription.isHidden();
        this.projectId = transcription.getProjectId();
        this.quota = transcription.getQuota() != null ? (int) (transcription.getQuota() / 1000000) : 0;
        this.usedSpaceQuota = transcription.getUsedSpaceQuota() != null ? (int) (transcription.getUsedSpaceQuota() / 1000000) : 0;
        this.percentOfTranscribed = transcription.getPercentOfTranscribed();
        this.userCount = transcription.getUserCount();
        this.layers = transcription.getLayers();
        this.pages = transcription.getPages();
        this.viewMode = transcription.getViewMode();
        this.percentOfVerified = transcription.getPercentOfVerified();
        this.continuation = transcription.getContinuation();
        this.asyncProgressInfo = transcription.getAsyncProgressInfo();
    }
}
