package pl.psnc.vtl.portal.model.transcriptions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.search.PostgreSQLTSVectorType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.ListUtils;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.List;

/**
 * Profile description metadata of transcription.
 */
@Entity
@Table(name = "profile_descs")
@Data
@NoArgsConstructor
@TypeDef(name = "tsvector", typeClass = PostgreSQLTSVectorType.class)
public class ProfileDesc {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "profile_desc_generator")
    @SequenceGenerator(name = "profile_desc_generator", sequenceName = "profile_descs_id_seq", allocationSize = 50)
    private Long id;

    /**
     * List of keywords.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> keywords;

    /**
     * Summary.
     */
    @Basic
    @Column(length = 8192)
    private String summary;

    /**
     * List of links.
     */
    @ElementCollection
    @Column(length = 2048)
    private List<String> links;

    /**
     * Field containing all keywords as single string.
     */
    @Column(length = 4096)
    @JsonIgnore
    private String keywordsString;

    /**
     * Field containing data for full text search.
     */
    @Column(columnDefinition = "tsvector", insertable = false, updatable = false)
    @JsonIgnore
    private String searchVector;

    /**
     * Updates keywordsString from list of all keywords.
     */
    @PrePersist
    @PreUpdate
    private void updateKeywords() {
        if(keywords != null) {
            keywordsString = String.join(" ", keywords);
        } else {
            keywordsString = null;
        }
    }

}
