package pl.psnc.vtl.ocrtraining.domain.training;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import pl.psnc.vtl.ocrtraining.domain.common.image.ImageMediaType;

import javax.persistence.*;

import java.time.LocalDateTime;

/**
 * Pending ocr correction object.
 */
@Entity
@Table(name = "pending_ocr_corrections")
@DynamicUpdate
@Data
@NoArgsConstructor
public class PendingOcrCorrection {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Url to image in iiif format.
     */
    @Column(name = "iiif_image_url", unique = true, nullable = false)
    private String iiifImageUrl;

    /**
     * Image format.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "image_format", nullable = false, length = 32)
    private ImageMediaType imageFormat;

    /**
     * Correct text.
     */
    @Column(name = "correct_text", nullable = false)
    private String correctText;

    /**
     * Text language.
     */
    @Column(name = "lang", nullable = false, length = 63)
    private String lang;

    /**
     * Number of fetch retries.
     */
    @Column(name = "fetch_retries_num", nullable = false)
    private int fetchRetriesNumber;

    /**
     * Last retry date.
     */
    @Column(name = "last_retry")
    private LocalDateTime lastRetry;

    /**
     * Queue time.
     */
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime queueTime;


    public PendingOcrCorrection(String iiifImageUrl, ImageMediaType imageFormat, String correctText, String lang) {
        this.id = null;
        this.iiifImageUrl = iiifImageUrl;
        this.imageFormat = imageFormat;
        this.correctText = correctText;
        this.lang = lang;
        this.fetchRetriesNumber = 0;
    }

    public PendingOcrCorrection(PendingOcrCorrection failedCorrection, LocalDateTime attemptTime) {
        this.id = null;
        this.iiifImageUrl = failedCorrection.getIiifImageUrl();
        this.imageFormat = failedCorrection.getImageFormat();
        this.correctText = failedCorrection.getCorrectText();
        this.lang = failedCorrection.getLang();
        this.lastRetry = attemptTime;
        this.fetchRetriesNumber = failedCorrection.getFetchRetriesNumber() + 1;
    }
}
