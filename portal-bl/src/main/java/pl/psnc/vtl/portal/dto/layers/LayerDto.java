package pl.psnc.vtl.portal.dto.layers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.layers.Layer;

/**
 * Layer dto object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LayerDto {

    /**
     * Layer id.
     */
    private Long id;

    /**
     * Layer name.
     */
    private String name;

    /**
     * Layer description.
     */
    private String description;

    private Layer.Type type;
}
