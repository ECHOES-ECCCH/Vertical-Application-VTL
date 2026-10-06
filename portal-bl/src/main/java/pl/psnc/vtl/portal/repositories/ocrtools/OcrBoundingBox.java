package pl.psnc.vtl.portal.repositories.ocrtools;

import lombok.AllArgsConstructor;
import lombok.Value;
import pl.psnc.vtl.portal.model.regions.BoundingBox;

@Value
@AllArgsConstructor
public class OcrBoundingBox {

    private long id;
    private int x1;
    private int x2;
    private int y1;
    private int y2;

    public OcrBoundingBox(long id, BoundingBox bbox) {
        this(id, bbox.getUpperLeftX(), bbox.getLowerRightX(), bbox.getUpperLeftY(), bbox.getLowerRightY());
    }
}
