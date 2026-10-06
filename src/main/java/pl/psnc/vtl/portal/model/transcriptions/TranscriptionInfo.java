package pl.psnc.vtl.portal.model.transcriptions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.search.PostgreSQLTSVectorType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TypeDef;
import pl.psnc.vtl.portal.utils.HTMLParser;

import javax.persistence.*;

/**
 * Metadata for Transcription
 */
@Entity
@Table(name = "transcription_infos")
@Data
@NoArgsConstructor
@TypeDef(name = "tsvector", typeClass = PostgreSQLTSVectorType.class)
public class TranscriptionInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transcription_info_generator")
    @SequenceGenerator(name = "transcription_info_generator", sequenceName = "transcription_infos_id_seq", allocationSize = 50)
    private Long id;

    /**
     * Release date of source material.
     */
    @Basic
    @Column(length = 4096)
    private String releaseDate;

    /**
     * Description of source material.
     */
    @Basic
    @Column(length = 4096)
    private String description;

    /**
     * Description of source material without formatting.
     */
    @Basic
    @Column(length = 4096)
    private String descriptionPlain;

    /**
     * Document types of source material.
     */
    @Basic
    @Column(length = 4096)
    private String documentTypes;

    /**
     * Document sources of source material.
     */
    @Basic
    @Column(length = 4096)
    private String documentSrc;

    /**
     * Document sources of source material without formatting.
     */
    @Basic
    @Column(length = 4096)
    private String documentSrcPlain;

    /**
     * Images sources of source material.
     */
    @Basic
    @Column(length = 4096)
    private String imgSrc;

    /**
     * Images sources of source material without formatting.
     */
    @Basic
    @Column(length = 4096)
    private String imgSrcPlain;

    /**
     * Collection name of source material.
     */
    @Basic
    @Column()
    private String collectionName;

    /**
     * Contact to transcription creator.
     */
    @Basic
    @Column()
    private String contactName;

    /**
     * Guidelines for project members.
     */
    @Basic
    @Column(length = 4096)
    private String guidelines;

    /**
     * Guidelines for project members without formatting.
     */
    @Basic
    @Column(length = 4096)
    private String guidelinesPlain;

    /**
     * Field containing data for full text search.
     */
    @Column(columnDefinition = "tsvector", insertable = false, updatable = false)
    @JsonIgnore
    private String searchVector;

    /**
     * Removes formatting and whitespaces from specified fields.
     */
    @PrePersist
    @PreUpdate
    public void removeWhitespace() {
        this.descriptionPlain = this.description != null ? HTMLParser.plainText(this.description) : null;
        this.documentSrcPlain = this.documentSrc != null ? HTMLParser.plainText(this.documentSrc) : null;
        this.guidelinesPlain = this.guidelines != null ? HTMLParser.plainText(this.guidelines) : null;
        this.imgSrcPlain = this.imgSrc != null ? HTMLParser.plainText(this.imgSrc) : null;
    }

}
