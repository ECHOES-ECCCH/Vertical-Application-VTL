package pl.psnc.vtl.portal.dto.indexes;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.dto.pages.PageDto;
import pl.psnc.vtl.portal.model.indexes.IndexType;

import java.util.ArrayList;
import java.util.List;

/**
 * Index dto object.
 */
@Data
@NoArgsConstructor
public class IndexDto {

    /**
     * Index id.
     */
    private Long id;

    /**
     * List of ids of verses that contain this index.
     */
    private List<Long> verseIds = new ArrayList<>();

    /**
     * List of ids of words that contain this index.
     */
    private List<Long> wordIds = new ArrayList<>();

    /**
     * Index name.
     */
    private String name;

    /**
     * Accessible on.
     */
    private String accessibleOn;

    /**
     * Index description.
     */
    private String description;

    /**
     * Index type.
     */
    private IndexType type;

    /**
     * List of pages dtos.
     */
    private List<PageDto> pages = new ArrayList<>();

    public void addVerseId(Long verseId) {
        this.verseIds.add(verseId);
    }

    public void addWordId(Long wordId) {
        this.wordIds.add(wordId);
    }

    public void addPageDto(PageDto pageDto) {
        this.pages.add(pageDto);
    }
}
