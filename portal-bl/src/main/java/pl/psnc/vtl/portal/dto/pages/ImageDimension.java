package pl.psnc.vtl.portal.dto.pages;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Image dimension object.
 */
@Data
@NoArgsConstructor
public class ImageDimension {

    /**
     * Image width.
     */
    private Integer width;

    /**
     * Image height.
     */
    private Integer height;
}
