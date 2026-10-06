package pl.psnc.vtl.portal.model.regions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Basic;
import javax.persistence.Embeddable;

/**
 * Verse bounding box on the page.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class BoundingBox {

    /**
     * Upper left X coordinate.
     */
    @Basic
    private Integer upperLeftX;

    /**
     * Upper left Y coordinate.
     */
    @Basic
    private Integer upperLeftY;

    /**
     * Lower right X coordinate.
     */
    @Basic
    private Integer lowerRightX;

    /**
     * Lower right Y coordinate.
     */
    @Basic
    private Integer lowerRightY;

    @Override
    public String toString() {
        return "box(" + upperLeftX + "," + upperLeftY + "->" + lowerRightX + "," + lowerRightY + ")";
    }
}
