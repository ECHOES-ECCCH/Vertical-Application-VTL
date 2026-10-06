package pl.psnc.vtl.portal.dto.layers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.layers.Layer;


/**
 *
 * Contains information about layer that should be created provided by the client (UI)
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LayerCreationDto {

    String name;
    String description;
    COPY copy = COPY.NONE;
    Layer.Type type;

    public enum COPY {
        VERSES_EMPTY, VERSES, BLOCKS_EMPTY, BLOCKS, NONE
    }

    public boolean versesCopyRequested() {
        return copy == COPY.VERSES_EMPTY ||
                copy == COPY.VERSES ||
                copy == COPY.BLOCKS ||
                copy == COPY.BLOCKS_EMPTY;
    }

    public boolean versesWithContentCopyRequested() {
        return copy == COPY.VERSES ||
                copy == COPY.BLOCKS;
    }

    public boolean blocksCopyRequested() {
        return copy == COPY.BLOCKS_EMPTY ||
                copy == COPY.BLOCKS;
    }
}
