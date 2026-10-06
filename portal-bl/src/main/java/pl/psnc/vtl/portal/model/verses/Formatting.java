package pl.psnc.vtl.portal.model.verses;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Verse formatting
 */
@Data
@NoArgsConstructor
@Embeddable
public class Formatting {

    /**
     * Forme work formatting.
     */
    @Basic
    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private FormeWork formeWork;

    /**
     * Heading formatting.
     */
    @Basic
    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private Heading heading;

    /**
     * Tabular form formatting.
     */
    @Basic
    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private TabularForm tabularForm;

    /**
     * List form formatting.
     */
    @Basic
    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private ListForm listForm;

    /**
     * Is verse marked as image caption.
     */
    @Basic
    private boolean imageCaption;

    /**
     * Is verse marked as notated music.
     */
    @Basic
    private boolean notatedMusic;

    /**
     * Is verse marked as end of paragraph.
     */
    @Basic
    private boolean endOfParagraph;

    /**
     * Is verse marked as bold.
     */
    @Basic
    private boolean bold;

    /**
     * Is verse marked as italic.
     */
    @Basic
    private boolean italic;

    /**
     * Is verse marked as underline.
     */
    @Basic
    private boolean underline;

    /**
     * Is verse marked as strike through.
     */
    @Basic
    private boolean strikethrough;

    /**
     * Is verse marked as annotation.
     */
    @Basic
    private boolean annotation;

    /**
     * Is verse marked as marginalia.
     */
    @Basic
    private boolean marginalia;


}