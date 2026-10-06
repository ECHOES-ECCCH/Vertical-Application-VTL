package pl.psnc.vtl.portal.model.layers;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Layer object.
 */
@Entity
@Table(name = "layers")
@Data
@NoArgsConstructor
public class Layer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "layers_generator")
    @SequenceGenerator(name = "layers_generator", sequenceName = "layers_id_seq", allocationSize = 50)
    private Long id;

    /**
     * Layer name.
     */
    @Basic
    @Column(nullable = false)
    private String name;

    /**
     * Layer description.
     */
    @Basic
    @Column(nullable = false, length = 4096)
    private String description;

    @Enumerated(EnumType.STRING)
    private Type type;

    /**
     * Transcription that this layer exists on.
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Transcription transcription;

    /**
     * Set of page layers that exists on that layer.
     */
    @OneToMany(mappedBy = "layer", orphanRemoval = true, fetch = FetchType.LAZY,  cascade=CascadeType.REMOVE)
    private Set<PageLayer> layerPages = new HashSet<>();

    public enum Type {
        IIIF_ANNOTATION;
    }
}
