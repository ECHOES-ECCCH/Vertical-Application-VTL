package pl.psnc.vtl.portal.model.transcriptions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.search.PostgreSQLTSVectorType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

import java.util.List;

/**
 * Monographic metadata od the project
 */
@Entity
@Table(name = "monografic_infos")
@Data
@NoArgsConstructor
@TypeDef(name = "tsvector", typeClass = PostgreSQLTSVectorType.class)
public class MonographicInfo{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "monografic_info_generator")
    @SequenceGenerator(name = "monografic_info_generator", sequenceName = "monografic_infos_id_seq", allocationSize = 50)
    private Long id;

    /**
     * Title of source material.
     */
    @Basic
    @Column(length = 4096)
    private String title;

    /**
     * Bibliographic type of source material.
     */
    @ManyToOne(optional = true, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(name = "bibl_type_code", referencedColumnName = "code")
    private BiblType biblType;

    /**
     * Actors of source material.
     */
    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinTable(name = "monografic_infos_actors",
            joinColumns = @JoinColumn(name = "monografic_info_id", referencedColumnName = "id", foreignKey = @ForeignKey(name="monografic_infos_actors_monografic_info_id_fk")),
            inverseJoinColumns = @JoinColumn(name = "actor_id", referencedColumnName = "id", foreignKey = @ForeignKey(name="monografic_infos_actors_actor_id_fk")))
    @OrderColumn(name = "ord")
    private List<Actor> actors;

    /**
     * Language of source material.
     */
    @ManyToOne(optional = true, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn
    private Language language;

    /**
     * Chapter number of source material.
     */
    @Basic
    @Column(length = 63)
    private String signature;

    @Basic
    private Integer chapterNo;

    /**
     * Volume number of source material.
     */
    @Basic
    private Integer volumeNo;

    /**
     * Issue number of source material.
     */
    @Basic
    private Integer issueNo;

    /**
     * Page range of source material.
     */
    @Basic
    private String pageRange;

    /**
     * Number of pages of source material.
     */
    @Basic
    private Integer pages;

    /**
     * Publisher of source material.
     */
    @Basic
    private String publisher;

    /**
     * Publication place of source meterial.
     */
    @Basic
    private String publicationPlace;

    /**
     * Publication date of source material.
     */
    @Basic
    private String publicationDate;

    /**
     * ISBN of source material.
     */
    @Basic
    private String isbn;

    /**
     * EISBN of source material.
     */
    @Basic
    private String eisbn;
    /**
     * ISSN of source material.
     */
    @Basic
    private String issn;

    /**
     * EISSN of source material.
     */
    @Basic
    private String eissn;

    /**
     * Field containing data for full text search.
     */
    @Column(columnDefinition = "tsvector", insertable = false, updatable = false)
    @JsonIgnore
    private String searchVector;
}
