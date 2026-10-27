package pl.psnc.vtl.portal.utils;

import lombok.experimental.UtilityClass;
import pl.psnc.vtl.portal.dto.regions.BoundingBoxCore;
import pl.psnc.vtl.portal.model.regions.BoundingBox;

import java.util.stream.Stream;

import static java.lang.Math.abs;
import static java.lang.Math.min;

@UtilityClass
public class BoundingBoxUtils {

    public BoundingBox convertBoundingBox(BoundingBoxCore boundingBox) {
        BoundingBox result = new BoundingBox();
        result.setUpperLeftX(boundingBox.getUpperLeftX());
        result.setUpperLeftY(boundingBox.getUpperLeftY());
        result.setLowerRightX(boundingBox.getLowerRightX());
        result.setLowerRightY(boundingBox.getLowerRightY());
        return result;
    }

    public BoundingBoxCore convertBoundingBox(BoundingBox boundingBox) {
        BoundingBoxCore result = new BoundingBoxCore();
        result.setUpperLeftX(boundingBox.getUpperLeftX());
        result.setUpperLeftY(boundingBox.getUpperLeftY());
        result.setLowerRightX(boundingBox.getLowerRightX());
        result.setLowerRightY(boundingBox.getLowerRightY());
        return result;
    }

    /**
     * @param boxStream
     * @return the smallest bounding box that contains all given boxes
     */
    public BoundingBox getContainingBox(Stream<BoundingBox> boxStream) {
        BoundingBox b = new BoundingBox(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        boxStream.forEach(b2 -> {
            b.setUpperLeftX(Math.min(b.getUpperLeftX(), b2.getUpperLeftX()));
            b.setUpperLeftY(Math.min(b.getUpperLeftY(), b2.getUpperLeftY()));
            b.setLowerRightX(Math.max(b.getLowerRightX(), b2.getLowerRightX()));
            b.setLowerRightY(Math.max(b.getLowerRightY(), b2.getLowerRightY()));
        });
        return b;
    }

    public long getArea(BoundingBox b) {
        return (long) (b.getLowerRightX() - b.getUpperLeftX()) * (b.getLowerRightY() - b.getUpperLeftY());
    }

    /**
     * @return Area of a bounding box that is a common part of both given boxes, or
     *         0 if given boxes have nothing in common
     */
    public long getCommonArea(BoundingBox b1, BoundingBox b2) {
        long cX = getCommonSegment(b1.getUpperLeftX(), b1.getLowerRightX(), b2.getUpperLeftX(), b2.getLowerRightX());
        long cY = getCommonSegment(b1.getUpperLeftY(), b1.getLowerRightY(), b2.getUpperLeftY(), b2.getLowerRightY());
        return cX * cY;
    }

    public int getCommonSegment(int left1, int right1, int left2, int right2) {
        int d1 = left1 - right2;
        int d2 = right1 - left2;
        if (d1 * d2 > 0)
            return 0;
        return min(min(abs(d1), abs(d2)),
                min(right1 - left1, right2 - left2));
    }

}
