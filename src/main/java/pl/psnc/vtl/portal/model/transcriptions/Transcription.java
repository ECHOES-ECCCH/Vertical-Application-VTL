package pl.psnc.vtl.portal.model.transcriptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pl.psnc.vtl.portal.conf.datetime.ApiDateTimeFormatter;
import pl.psnc.vtl.portal.dto.pages.PaginatedPages;
import pl.psnc.vtl.portal.dto.permissions.ViewMode;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionContinuation;
import pl.psnc.vtl.portal.model.imports.FileType;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.services.eBook.AsyncTaskResponse;
import pl.psnc.vtl.portal.services.layers.CustomLayerListSerializer;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Transcription object
 */
@Data
@Entity
@Table(name = "transcriptions", indexes = {  @Index(columnList = "projectId")})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transcription {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transcription_generator")
    @SequenceGenerator(name = "transcription_generator", sequenceName = "transcriptions_id_seq", allocationSize = 50)
    private Long id;

    /**
     * Title of the transcription.
     */
    @Basic
    @Column(length = 4096)
    private String titleLabel;

    /**
     * Author of imported transcription.
     */
    @Basic
    @Column(nullable = true, length = 4096)
    private String authorsLabel;

    /**
     * Project creation date.
     */
    @Basic
    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.dateTimePattern)
    private LocalDateTime dateCreated;

    /**
     * Project publication date.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.dateTimePattern)
    private LocalDateTime datePublished;

    /**
     * User that created the transcription. They are also the owner of the transcription.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH })
    @JoinColumn
    private User creator;

    /**
     * Date the transcription was last modified.
     */
    @UpdateTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.dateTimePattern)
    private LocalDateTime lastModificationDate;

    /**
     * Creation mode of the transcription.
     * It specifies if the transcription was created using web form or was imported from specific manifest format.
     */
    @Basic
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CreationMode creationMode;

    /**
     * Status of the transcription that was imported from dLibra.
     * Status tells that if transcription is newly imported, ready or accepted to export back.
     */
    @Basic
    @Enumerated(EnumType.STRING)
    private TranscriptionExportStatus exportStatus;

    /**
     * Url from which transcription was imported.
     */
    @Basic
    @Column(nullable = true)
    private String externalUrl;

    @Basic
    @Column(nullable = true)
    private String externalMetadataUrl;

    @Basic
    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private FileType externalMetadataType;

    /**
     * Id of the transcription in the external source.
     */
    @Basic
    @Column(nullable = true)
    private String externalId;

    /**
     * Analytic info metadata.
     */
    @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn
    private AnalyticInfo analyticInfo;

    /**
     * Monographic info metadata.
     */
    @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn
    private MonographicInfo monographicInfo;

    /**
     * Set of metadata that is filled on transcription creation.
     */
    @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn
    private TranscriptionInfo transcriptionInfo;

    /**
     * Profile description metadata.
     */
    @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn
    private ProfileDesc profileDesc;

    /**
     * Publication status of the project.
     * When project is hidden than it is not public.
     */
    @Basic
    @Column(nullable = false)
    private boolean hidden;


    /**
     * System unique object identifier.
     */
    @Basic
    @Column(nullable = true)
    private String projectId;

    /**
     * Header of the TEI file if project was imported from TEI.
     */
    @Lob
    @Column(nullable = true)
    private String teiHeader;

    /**
     * Body of the TEI file if project was imported from TEI.
     */
    @Lob
    @Column(nullable = true)
    private String teiBody;

    /**
     * Size limit of project files.
     */
    @Basic
    @Column(nullable = true)
    private Long quota;

    /**
     * Amount of used up space for project files.
     */
    @Basic
    @Column(nullable = false)
    private Long usedSpaceQuota = 0L;

    /**
     * Percentage of verses that are marked as transcribed.
     */
    @Column(nullable = false)
    private Integer percentOfTranscribed = 0;

    /**
     * Number of members in the transcription.
     */
    @Transient
    private Integer userCount;

    /**
     * List of transcriptions layers.
     */
    @Transient
    @JsonSerialize(using = CustomLayerListSerializer.class)
    private List<Layer> layers;

    /**
     * Paginated list of transcription pages.
     */
    @Transient
    private PaginatedPages pages;

    /**
     * Current user view mode of the cranscription.
     */
    @Transient
    private ViewMode viewMode;

    /**
     * Percentage of transcriptions verses marked as verified.
     */
    @Transient
    private Integer percentOfVerified;

    /**
     * lastSetLanguageCode
     */
    @JsonIgnore
    private String lastSetLanguageCode;

    /**
     * transcriptionContinuation
     */
    @Transient
    private TranscriptionContinuation continuation;

    /**
     * Tells if transcription is being exported to single file.
     */
    @Transient
    private boolean toSingleFileExport;

    /**
     * Progress of async operation that is being performed over transcription.
     */
    @Transient
    private AsyncTaskResponse asyncProgressInfo;

}
