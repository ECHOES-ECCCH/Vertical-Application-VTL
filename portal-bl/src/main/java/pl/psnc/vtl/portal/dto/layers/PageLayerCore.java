package pl.psnc.vtl.portal.dto.layers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Page layer core object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageLayerCore {

    /**
     * Layer name.
     */
    String name;

    /**
     * Layer description.
     */
    String description;

    /**
     * Page id.
     */
    Long pageId;

    /**
     * Layer id.
     */
    Long layerId;
}
