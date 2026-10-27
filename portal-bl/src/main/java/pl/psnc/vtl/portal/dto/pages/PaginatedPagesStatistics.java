package pl.psnc.vtl.portal.dto.pages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated page statistics object.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedPagesStatistics {

    /**
     * Total page statistics hits.
     */
    private long hits;

    /**
     * Page statistics count on current page.
     */
    private int count;

    /**
     * Current page number.
     */
    private int page;

    /**
     * Maximum number of page statistics per page.
     */
    private int perPage;

    /**
     * Total number of pages.
     */
    private int pages;

    /**
     * Layer id.
     */
    private long layerId;

    /**
     * List of page statistics on current page.
     */
    List<PageStatistics> pageStatisticsList;
}
