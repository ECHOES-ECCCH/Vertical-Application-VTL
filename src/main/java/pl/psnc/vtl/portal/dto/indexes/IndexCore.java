package pl.psnc.vtl.portal.dto.indexes;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import pl.psnc.vtl.portal.model.indexes.IndexType;

/**
 * Index core object.
 */
@Data
@NoArgsConstructor
public class IndexCore {

    /**
     * Index name.
     */
    String name;

    /**
     * Index type.
     */
    IndexType indexType;

    /**
     * Accessible on.
     */
    String accessibleOn;

    /**
     * Index description.
     */
    String description;

}
