package pl.psnc.vtl.portal.model.pages;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;
import pl.psnc.vtl.portal.conf.datetime.ApiDateTimeFormatter;
import pl.psnc.vtl.portal.dto.permissions.ViewMode;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.verses.StudentVerse;
import pl.psnc.vtl.portal.model.verses.Verse;
import javax.annotation.Nullable;
import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Page object.
 */
@Entity
@Table(name = "pages")
@Data
@NoArgsConstructor
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "page_generator")
    @SequenceGenerator(name = "page_generator", sequenceName = "pages_id_seq", allocationSize = 50)
    private Long id;

    /**
     * Transcription that this page is part of.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Transcription transcription;

    /**
     * List of page layers that are placed on this page.
     */
    @OneToMany(mappedBy = "page", fetch = FetchType.LAZY, orphanRemoval = true, cascade = {CascadeType.ALL})
    private List<PageLayer> pageLayers = new ArrayList<>();

    /**
     * Page path.
     */
    @Basic
    @Column(nullable = false)
    private String path;

    /**
     * Page number in the transcription.
     */
    @Basic
    @Column(nullable = false)
    private int pageNo;

    /**
     * Relative page number.
     */
    @Basic
    @Column(nullable = true)
    private Integer relativeNo;

    /**
     * Page bounding box.
     */
    @Embedded
    private BoundingBox boundingBox;

    /**
     * IIIF source url
     */
    @Basic
    @Column(nullable = false)
    @JsonIgnore
    private String iiifUrl;

    /**
     * Url to page thumbnail.
     */
    @Basic
    @Column(nullable = false)
    private String thumbnailUrl;

    /**
     * List of verses on this page.
     */
    @Transient
    private List<Verse> verses;

    /**
     * List of student verses on this page.
     */
    @Transient
    private List<StudentVerse> studentVerses;

    /**
     * Page title.
     */
    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String titleLabel;

    /**
     * Previous page id.
     */
    @Transient
    private Long prevPageId;

    /**
     * Next page id.
     */
    @Transient
    private Long nextPageId;

    /**
     * Current user view mode of this page.
     */
    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ViewMode viewMode;

    /**
     * Transcription id.
     */
    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long transcriptionIdForEvent;

    /**
     * Percent of transcribed verses on this page.
     */
    @Basic
    @Column
    @Nullable
    private Integer percentOfTranscribed;

    /**
     * Percent of verified verses on this page.
     */
    @Basic
    @Column
    @JsonIgnore
    private Integer percentOfVerified;

    /**
     * Is this page marked as empty so that it's ignored when checking transcription
     * progress.
     */
    @Basic
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean empty;

    /**
     * Name of user that lastly modified this page.
     */
    @Basic
    @Column(nullable = true)
    @JsonIgnore
    private String lastModifier;

    /**
     * Page last modification date.
     */
    @UpdateTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.dateTimePattern)
    private LocalDateTime lastModificationDate;

    /**
     * Field corresponding to the TEI <i>page beginning</i> property.
     * <a href="https://tei-c.org/release/doc/tei-p5-doc/en/html/ref-pb.html">docs</a>
     */
    private String teiPbValue;
}
