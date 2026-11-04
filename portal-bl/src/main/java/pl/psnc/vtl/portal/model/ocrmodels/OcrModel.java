package pl.psnc.vtl.portal.model.ocrmodels;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.psnc.vtl.portal.model.users.User;

import javax.persistence.*;

@Entity
@Table(name = "ocr_models")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OcrModel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ocr_model_generator")
    @SequenceGenerator(name = "ocr_model_generator", sequenceName = "ocr_models_id_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Three letter language code (ISO 639-3) or null if the model is multilingual
     */
    @Column(nullable = true, length = 8)
    private String language;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH })
    @JoinColumn
    private User owner;

    @Basic
    @Enumerated(EnumType.STRING)
    private Engine engine;

    @Column(nullable = false)
    private boolean isPublic;

    public enum Engine {
        TESSERACT,
        KRAKEN;

        @JsonValue
        public String getValue() {
            return name().replace('_', '-').toLowerCase();
        }

        @Override
        public String toString() {
            return getValue();
        }
    }
}
