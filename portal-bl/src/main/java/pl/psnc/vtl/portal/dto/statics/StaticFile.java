package pl.psnc.vtl.portal.dto.statics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Static file object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaticFile {

    /**
     * En filename.
     */
    private String en;

    /**
     * Pl filename.
     */
    private String pl;

    /**
     * De filename.
     */
    private String de;
}
