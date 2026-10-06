package pl.psnc.vtl.portal.dto.transcriptions;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Monographic info core object
 */
@Data
@NoArgsConstructor
public class MonographicInfoCore {

    /**
     * Source title.
     */
    private String title;

    /**
     * Source bibliographic type.
     */
    private BiblTypeId biblType;

    /**
     * Source list of actors.
     */
    private List<ActorCore> actors;

    /**
     * Source language.
     */
    private LanguageId language;

    /**
     * Source signature
     */
    private String signature;

    /**
     * Source chapter number.
     */
    private Integer chapterNo;

    /**
     * Source volume number.
     */
    private Integer volumeNo;

    /**
     * Source issue number.
     */
    private Integer issueNo;

    /**
     * Source page range.
     */
    private String pageRange;

    /**
     * Source number of pages.
     */
    private Integer pages;

    /**
     * Source publisher.
     */
    private String publisher;

    /**
     * Source publication place.
     */
    private String publicationPlace;

    /**
     * Source publication date.
     */
    private String publicationDate;

    /**
     * Source ISBN.
     */
    private String isbn;

    /**
     * Source EISBN.
     */
    private String eisbn;

    /**
     * Source ISSN.
     */
    private String issn;

    /**
     * Source EISSN.
     */
    private String eissn;

}
