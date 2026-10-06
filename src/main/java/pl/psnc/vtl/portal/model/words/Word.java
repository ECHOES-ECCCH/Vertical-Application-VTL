package pl.psnc.vtl.portal.model.words;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import pl.psnc.vtl.portal.model.indexes.Index;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.model.verses.Verse;

import javax.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "words")
@Data
@NoArgsConstructor
public class Word implements Comparable<Word> {

    public static final int MAX_LENGTH = 253;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "words_generator")
    @GenericGenerator(name = "words_generator",
            strategy = "pl.psnc.vtl.portal.repositories.persistence.RestorableSequenceGenerator",
            parameters = { @Parameter(name = "sequence_name", value = "words_id_seq") })
    private Long id;

    @Column(length = 253)
    private String content;

    @Embedded
    private BoundingBox boundingBox;

    /**
     * Field corresponding to the TEI <i>gap</i> element.
     * <a href="https://tei-c.org/release/doc/tei-p5-doc/en/html/ref-gap.html">docs</a>
     */
    private boolean teiGap;

    private boolean teiDel;

    /**
     * Field containing value of <b><i>place</i></b> attribute for TEI <b><i>add</i></b> element
     */
    private String teiAdd;

    /**
     * <p>Field indicating that given word is part of the TEI <b><i>subst</i></b> (substitution) element.</p>
     * <p>Words with the same field value belongs to the same <b><i>subst</i></b> element.</p>
     */
    private String teiSubst;

    @Basic
    private Integer ordering;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH })
    @JoinColumn
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Verse verse;

    /**
     * Indexes connected with this word.
     */
    @ManyToMany(cascade = { CascadeType.REFRESH })
    @JoinTable(name = "word_index",
        joinColumns = { @JoinColumn(name = "word_id") },
        inverseJoinColumns = { @JoinColumn(name = "index_id") })
    @EqualsAndHashCode.Exclude
    private Set<Index> indexes = new HashSet<>();

    public void addIndex(Index index) {
        indexes.add(index);
    }

    public void removeIndex(Index index) {
        indexes.remove(index);
    }

    @Override
    public int compareTo(Word o) {
        int dx = this.boundingBox.getLowerRightX() + this.boundingBox.getUpperLeftX()
                - o.boundingBox.getLowerRightX() - o.boundingBox.getUpperLeftX();
        int dy = this.boundingBox.getLowerRightY() + this.boundingBox.getUpperLeftY()
                - o.boundingBox.getLowerRightY() - o.boundingBox.getUpperLeftY();
        return Math.abs(dx) > Math.abs(dy) ? dx : dy;
    }

    public void setContent(String content) {
        if (content.length() > MAX_LENGTH) {
            this.content = content.substring(0, MAX_LENGTH);
        } else {
            this.content = content;
        }
    }
}
