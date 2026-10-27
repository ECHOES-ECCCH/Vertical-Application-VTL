package pl.psnc.vtl.portal.model.indexes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.search.PostgreSQLTSVectorType;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.TypeDef;
import org.springframework.lang.Nullable;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.words.Word;

import javax.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Index object.
 */
@Entity
@Table(name = "indexes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TypeDef(name = "tsvector", typeClass = PostgreSQLTSVectorType.class)
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "indexes_generator")
    @GenericGenerator(name = "indexes_generator",
            strategy = "pl.psnc.vtl.portal.repositories.persistence.RestorableSequenceGenerator",
            parameters = { @Parameter(name = "sequence_name", value = "indexes_id_seq") })
    private Long id;

    /**
     * Index of transcription that this index was created for.
     */
    @Basic
    @Column(nullable = false)
    private Long transcriptionId;

    /**
     * Set of verses marked with this index.
     */
    @JsonIgnore
    @ManyToMany(mappedBy = "indexes", cascade = { CascadeType.REFRESH })
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Verse> verses = new HashSet<>();

    /**
     * Set of words marked with this index.
     */
    @JsonIgnore
    @ManyToMany(mappedBy = "indexes", cascade = { CascadeType.REFRESH })
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Word> words = new HashSet<>();

    /**
     * Type of the index.
     */
    @Basic
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private IndexType type;

    /**
     * Name of the index.
     */
    @Basic
    @Column(nullable = false)
    private String name;

    /**
     * Accessible on. A URL to geolocation entity, currently only used for indexes
     * of type PLACE.
     */
    @Basic
    @Column(nullable = true)
    private String accessibleOn;

    /**
     * Index description.
     */
    @Basic
    @Column(nullable = true, length = 4096)
    @Nullable
    private String description;

    /**
     * Field containing data for full text search.
     */
    @Column(columnDefinition = "tsvector", insertable = false, updatable = false)
    @JsonIgnore
    private String searchVector;

    public void addVerse(Verse verse) {
        this.verses.add(verse);
    }

    public void removeVerse(Verse verse) {
        this.verses.remove(verse);
    }

    public void addWord(Word word) {
        this.words.add(word);
    }

    public void removeWord(Word word) {
        this.words.remove(word);
    }

    public void replaceVerse(Verse verse) {
        if (this.verses != null)
            this.verses.clear();
        this.verses.add(verse);
    }
}
