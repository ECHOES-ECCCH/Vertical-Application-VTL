package pl.psnc.vtl.portal.dto.pages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.pages.Page;

import java.util.List;

/**
 * Paginated pages object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedPages {

    /**
     * Total pages hits.
     */
    private long hits;

    /**
     * Number of pages on current page.
     */
    private int count;

    /**
     * Current page number.
     */
    private int page;

    /**
     * Maximum number of pages on page.
     */
    private int perpage;

    /**
     * Total number of pages.
     */
    private int pages;

    /**
     * List of pages on current page.
     */
    private List<Page> list;

}
