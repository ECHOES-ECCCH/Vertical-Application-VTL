package pl.psnc.vtl.portal.dto.transcriptions;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Profile description core object.
 */
@Data
@NoArgsConstructor
public class ProfileDescCore {

    /**
     * List of keywords.
     */
    private List<String> keywords;

    /**
     * Summary.
     */
    private String summary;

    /**
     * List of links.
     */
    private List<String> links;

}
