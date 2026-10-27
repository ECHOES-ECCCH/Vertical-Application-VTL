package pl.psnc.vtl.portal.dto.transcriptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Paginated transcription object.
 * @param <T> Type of transcription representation object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PaginatedTranscriptions<T> {

    /**
     * Total transcription hit count.
      */
    private long hits;

    /**
     * Number of transcriptions on current page.
     */
    private int count;

    /**
     * Current page number.
     */
    private int page;

    /**
     * Maximum number of transcriptions on single page.
     */
    private int perpage;

    /**
     * Number of pages.
     */
    private int pages;

    /**
     * List of transcriptions on current page.
     */
    private List<T> transcriptions;

}
