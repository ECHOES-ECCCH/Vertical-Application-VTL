package pl.psnc.vtl.portal.model.verses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Content of the verse created by the student user.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentVerseContent {

    /**
     * Verse id that this content corresponds to.
     */
    private Long verseId;

    /**
     * Content of the student verse.
     */
    private String content;
}
