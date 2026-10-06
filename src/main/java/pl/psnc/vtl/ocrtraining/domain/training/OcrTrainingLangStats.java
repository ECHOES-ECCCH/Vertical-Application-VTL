package pl.psnc.vtl.ocrtraining.domain.training;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

import java.time.LocalDateTime;


/**
 * Ocr training language statistics.
 */
@Entity
@Table(name = "ocr_training_lang_stats")
@DynamicUpdate
@Getter
@NoArgsConstructor
class OcrTrainingLangStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Version.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /**
     * Language.
     */
    @Column(name = "lang", nullable = false, unique = true, length = 63)
    private String lang;

    /**
     * Total number of examples.
     */
    @Column(name = "total_examples", nullable = false)
    private long totalExamples;

    /**
     * Number of used examples.
     */
    @Column(name = "used_examples", nullable = false)
    private long usedExamples;

    /**
     * Number of new examples.
     */
    @Column(name = "new_examples", nullable = false)
    private long newExamples;

    /**
     * Last training date.
     */
    @Column(name = "last_training")
    private LocalDateTime lastTraining;

    public OcrTrainingLangStats(String lang) {
        this.id = null;
        this.version = null;
        this.lang = lang;

        this.totalExamples = 0;
        this.usedExamples = 0;
        this.newExamples = 0;

        this.lastTraining = null;
    }


    public void notifyNewExamples(long newExamplesCount) {
        totalExamples += newExamplesCount;
        newExamples += newExamplesCount;
    }

    public void notifyUsedExamples(long usedExamplesCount) {
        if (usedExamples + usedExamplesCount > totalExamples)
            throw new IllegalStateException("Number of used examples exceeds total number of training examples");

        if (newExamples < usedExamplesCount)
            throw new IllegalStateException("Cannot use more than the number of available new examples");

        usedExamples += usedExamplesCount;
        newExamples -= usedExamplesCount;
    }
}
