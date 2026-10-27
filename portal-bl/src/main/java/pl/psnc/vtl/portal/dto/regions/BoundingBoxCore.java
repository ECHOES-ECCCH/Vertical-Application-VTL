package pl.psnc.vtl.portal.dto.regions;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BoundingBoxCore {

    /**
     * Upper left X coordinate.
     */
    private Integer upperLeftX;

    /**
     * Upper left Y coordinate.
     */
    private Integer upperLeftY;

    /**
     * Lower right X coordinate.
     */
    private Integer lowerRightX;

    /**
     * Lower right Y coordinate.
     */
    private Integer lowerRightY;

}
