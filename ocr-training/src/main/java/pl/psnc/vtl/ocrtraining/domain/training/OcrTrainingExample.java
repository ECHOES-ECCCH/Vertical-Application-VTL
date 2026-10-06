package pl.psnc.vtl.ocrtraining.domain.training;

import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.ocrtraining.domain.common.image.ImageMediaType;

import javax.persistence.*;

import java.nio.file.Path;


/**
 * Ocr training example.
 */
@Entity
@Table(name = "ocr_training_examples", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "iiif_img_url", "lang" })
})
@Getter
@NoArgsConstructor
class OcrTrainingExample {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Url to image in iiif format.
     */
    @Column(name = "iiif_img_url", nullable = false)
    private String iiifImgUrl;

    /**
     * Path to ocr text.
     */
    @Column(name = "ocr_text_path", nullable = false)
    private Path ocrTextPath;

    /**
     * Ocr language.
     */
    @Column(name = "lang", nullable = false, length = 63)
    private String lang;

    /**
     * Path to the image.
     */
    @Column(name = "image_path", nullable = false, unique = true)
    private Path imagePath;

    /**
     * Format of the image.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "image_format", nullable = false, length = 32)
    private ImageMediaType imageFormat;


    public OcrTrainingExample(Path ocrTextPath, String iiifImgUrl, String lang, Path imagePath,
            ImageMediaType imageFormat) {
        this.ocrTextPath = ocrTextPath;
        this.iiifImgUrl = iiifImgUrl;
        this.lang = lang;
        this.imagePath = imagePath;
        this.imageFormat = imageFormat;
    }
}
