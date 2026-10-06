package pl.psnc.vtl.portal.model.transcriptions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.search.PostgreSQLTSVectorType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.List;

/**
 * Analytic metadata of the transcription
 */
@Entity
@Table(name = "analytic_infos")
@Data
@NoArgsConstructor
@TypeDef(name = "tsvector", typeClass = PostgreSQLTSVectorType.class)
public class AnalyticInfo{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "analytic_info_generator")
    @SequenceGenerator(name = "analytic_info_generator", sequenceName = "analytic_infos_id_seq", allocationSize = 50)
    private Long id;

    /**
     * Title of the source material.
     */
    @Basic
    @Column(length = 4096)
    private String title;

    /**
     * Actors of the source material.
     */
    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinTable(name = "analytic_infos_actors",
            joinColumns = @JoinColumn(name = "analytic_info_id", referencedColumnName = "id", foreignKey = @ForeignKey(name="analytic_infos_actors_analytic_info_id_fk")),
            inverseJoinColumns = @JoinColumn(name = "actor_id", referencedColumnName = "id", foreignKey = @ForeignKey(name="analytic_infos_actors_actor_id_fk")))
    @OrderColumn(name = "ord")
    private List<Actor> actors;

    /**
     * Language of the source material.
     */
    @ManyToOne(optional = true, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn
    private Language language;

    /**
     * Field containing data for full text search.
     */
    @Column(columnDefinition = "tsvector", insertable = false, updatable = false)
    @JsonIgnore
    private String searchVector;
}
