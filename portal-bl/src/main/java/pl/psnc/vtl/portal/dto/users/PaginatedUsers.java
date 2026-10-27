package pl.psnc.vtl.portal.dto.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated list of users.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedUsers {

    /**
     * Total hit count.
     */
    private long hits;

    /**
     * Number of users on given page.
     */
    private int count;

    /**
     * Current page number
     */
    private int page;

    /**
     * Max user count on single page.
     */
    private int perPage;

    /**
     * Total number of pages.
     */
    private int pages;

    /**
     * List of users on current page.
     */
    private List<UserRegularDto> users;

}
