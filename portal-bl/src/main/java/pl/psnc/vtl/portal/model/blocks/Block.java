package pl.psnc.vtl.portal.model.blocks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Parameter;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import javax.persistence.*;

@Entity
@Table(name = "blocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "blocks_generator")
    @GenericGenerator(name = "blocks_generator",
            strategy = "pl.psnc.vtl.portal.repositories.persistence.RestorableSequenceGenerator",
            parameters = { @Parameter(name = "sequence_name", value = "blocks_id_seq") })
    private Long id;

    @Basic
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BlockType type;

    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH })
    @JoinColumn(name = "page_layer_page_id", referencedColumnName = "page_id")
    @JoinColumn(name = "page_layer_layer_id", referencedColumnName = "layer_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PageLayer pageLayer;

    @Basic
    @Column(nullable = false)
    private Integer firstVerseNo;

    @Basic
    @Column(nullable = false)
    private Integer lastVerseNo;

    /** Is this block a continuation from the previous page? */
    @Basic
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean continuation;
}
