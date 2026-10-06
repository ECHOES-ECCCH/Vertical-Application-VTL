package pl.psnc.vtl.portal.model.verses;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.model.verses.Verse.Type;

/**
 * Verse created by the student user.
 */
@Data
@NoArgsConstructor
public class StudentVerse {

    private Long id;

    private Long layerId;

    /**
     * Verse number on the page.
     */
    private Integer verseNo;

    /**
     * Verse content.
     */
    private String content;

    /**
     * Verse bounding box.
     */
    private BoundingBox boundingBox;

    private Type type;

    /**
     * Is verse an image.
     */
    private boolean image;

    /**
     * Learning status of the verse.
     */
    private LearningStatus learningStatus;

    @Override
    public String toString() {
        return "Verse(id=\"" +
                this.getId() +
                ", verseNo=" +
                this.getVerseNo() +
                ", content=" +
                this.getContent() +
                ", boundingBox=" +
                this.getBoundingBox() +
                ", image=" +
                this.isImage() +
                ", learningStatus=" +
                this.getLearningStatus() +
                ")";
    }
}
