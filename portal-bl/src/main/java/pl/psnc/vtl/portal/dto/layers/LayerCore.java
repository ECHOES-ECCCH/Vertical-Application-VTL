package pl.psnc.vtl.portal.dto.layers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.layers.Layer;

/**
 * Layer core object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LayerCore {

    /**
     * Transcription id.
     */
    Long transcriptionId;

    /**
     * Layer name.
     */
    String name;

    /**
     * Layer description.
     */
    String description;
    Layer.Type type;
}
