package pl.psnc.vtl.portal.model.verses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vladmihalcea.hibernate.type.search.PostgreSQLTSVectorType;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.TypeDef;
import org.springframework.lang.Nullable;
import pl.psnc.vtl.portal.model.events.Event;
import pl.psnc.vtl.portal.model.indexes.Index;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.words.Word;
import pl.psnc.vtl.portal.services.layers.CustomPageLayerSerializer;
import pl.psnc.vtl.portal.utils.HTMLParser;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Verse object.
 */
@Entity
@Table(name = "verses", indexes = @javax.persistence.Index(
        name = "verse_status_layer_index", columnList = "status, page_layer_page_id, page_layer_layer_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@TypeDef(name = "tsvector", typeClass = PostgreSQLTSVectorType.class)
@Builder
public class Verse {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "verses_generator")
    @GenericGenerator(name = "verses_generator",
            strategy = "pl.psnc.vtl.portal.repositories.persistence.RestorableSequenceGenerator",
            parameters = { @Parameter(name = "sequence_name", value = "verses_id_seq") })
    private Long id;

    /**
     * Page that this verse belongs to.
     */
    //    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH})
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH})
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    @JoinColumn(name = "page_id")
    @EqualsAndHashCode.Exclude
    private Page page;

    /**
     * Page layer that this verse belongs to.
     */
    @ManyToOne(cascade = {CascadeType.REFRESH})
    @JoinColumns ( {
            @JoinColumn(name = "page_layer_page_id", referencedColumnName = "page_id"),
            @JoinColumn(name = "page_layer_layer_id", referencedColumnName = "layer_id")
    })
    @Nullable
    @JsonSerialize(using = CustomPageLayerSerializer.class)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @EqualsAndHashCode.Exclude
    private PageLayer pageLayer;

    /**
     * Verse number on the page.
     */
    @Basic
    @Column(nullable = false)
    private Integer verseNo;

    /** Words this verse consists of, if applicable */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "verse", cascade = CascadeType.ALL)
    @OrderBy("ordering")
    @Builder.Default
    private List<Word> words = new ArrayList<>();

    /**
     * Initial content of this verse.
     */
    @Basic
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JsonIgnore
    private VerseContent initialVerseContent;

    /**
     * Current content of this verse.
     */
    @Basic
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JsonIgnore
    private VerseContent currentVerseContent;

    /**
     * Payload that can be attached to verse. For now, it will contain xml files with music notation, but can be used for any payload.
     */
    @Embedded
    private VersePayload payload;

    public void setCurrentVerseContent(VerseContent currentVerseContent) {
        if (currentVerseContent != null && currentVerseContent.getContent() != null) {
            if (this.initialVerseContent == null) {
                currentVerseContent.setInitial(true);
                this.initialVerseContent = currentVerseContent;
            }
            this.content = currentVerseContent.getContent();
            this.removeWhitespace();
        }
        this.currentVerseContent = currentVerseContent;
    }

    /**
     * Language of the verse.
     */
    @Column
    private String lang;

    /**
     * Bounding box of the verse.
     */
    @Embedded
    private BoundingBox boundingBox;

    /**
     * Verse formatting.
     */
    @Embedded
    private Formatting formatting;

    /**
     * Is this verse an image.
     */
    @Basic
    private boolean image;

    @Enumerated(EnumType.STRING)
    private Type type;

    /**
     * Author of last modification.
     */
    @ManyToOne(optional = true, fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH})
    @JsonIgnore
    private User authorOfLastStatusModification;

    /**
     * Author of initial content.
     */
    @ManyToOne(optional = true, fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH})
    @JsonIgnore
    private User author;

    /**
     * Verse status.
     */
    @Basic
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VerseStatus status;

    /**
     * Verse status in simple string form.
     */
    @Basic
    @Column(nullable = true,  length = 4096)
    private String content;

    /**
     * Verse status in simple plain string form.
     */
    @Basic
    @Column(nullable = true, length = 4096)
    private String plainContent;

    /**
     * Is content of this verse from OCR.
     */
    @Basic
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isFromOCR;

    /**
     * Number of comments to this verse.
     */
    @Transient
    private int comments;

    /**
     * Is this verse active.
     */
    @Transient
    private boolean active;

    @ManyToOne(optional = true, fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH })
    @JsonIgnore
    private User modificationLastEventAttendee;

    @ManyToOne(optional = true, fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH })
    @JsonIgnore
    private Event modificationLastEvent;

    /*
     * @return last stored content
     */
    @Transient
    public String currentContent() {
        String content = null;
        if (this.getCurrentVerseContent() != null) {
            content = this.getCurrentVerseContent().getContent();
        }
        if (content != null) {
            return content;
        }

        if (this.content != null) {
            return this.content;
        }
        return "";
    }

    /**
     * Field containing data for full text search.
     */
    @Column(columnDefinition = "tsvector", insertable = false, updatable = false)
    @JsonIgnore
    private String searchVector;

    /**
     * Indexes connected with this verse.
     */
    @ManyToMany(cascade = { CascadeType.REFRESH })
    @JoinTable(name = "verse_index",
            joinColumns = { @JoinColumn(name = "verse_id") },
            inverseJoinColumns = { @JoinColumn(name = "index_id") })
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private Set<Index> indexes = new HashSet<>();

    public void addIndex(Index index) {
        indexes.add(index);
    }

    public void removeIndex(Index index) {
        indexes.remove(index);
    }

    @Override
    public String toString() {
        return currentContent() + " (no:" + this.getVerseNo() + ", id:" + this.getId() + ", "
                + this.getBoundingBox() + ")";
    }

    @PrePersist
    @PreUpdate
    public void removeWhitespace() {
        this.plainContent = this.content != null ? HTMLParser.plainText(this.content) : null;
    }

    /**
     * <p>Describes type of verse.</p>
     * <p>Currently most of verses have no type. We can specify MUSIC_NOTATION type
     *  that indicates that verse should contain payload with MusicXML.</p>
     */
    public enum Type {
        MUSIC_NOTATION;
    }
}
