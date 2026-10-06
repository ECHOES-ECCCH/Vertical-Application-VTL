package pl.psnc.vtl.portal.controllers.verses;

import lombok.Data;
import pl.psnc.vtl.portal.dto.regions.BoundingBoxCore;
import pl.psnc.vtl.portal.model.verses.Verse;

/**
 *Holds parameters related with new verse creation. For now these parameters are @{@link BoundingBoxCore} and {@link Verse.Type}
 */
@Data
public class VerseCreationDto {

    private BoundingBoxCore boundingBox;
    private Verse.Type type;
}
