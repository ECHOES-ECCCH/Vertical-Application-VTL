package pl.psnc.vtl.portal.dto.verses;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Comment core containing its content.
 */
@Data
@NoArgsConstructor
public class CommentCore {

    /**
     * Comment content.
     */
    private String content;

}
