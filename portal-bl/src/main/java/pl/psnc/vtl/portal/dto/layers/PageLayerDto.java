package pl.psnc.vtl.portal.dto.layers;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.layers.CustomVerseListSerializer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Page layer dto object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageLayerDto {

    /**
     * Layer id.
     */
    private Long layerId;

    /**
     * Page id.
     */
    private Long pageId;

    /**
     * Layer name.
     */
    private String name;

    /**
     * List od verses on this page layer.
     */
    @JsonManagedReference
    @JsonSerialize(using = CustomVerseListSerializer.class)
    private List<Verse> verses;

    /**
     * Percent of transcribed verses.
     */
    @Nullable
    private Integer percentOfTranscribed;

    /**
     * Percent of verified verses.
     */
    @Nullable
    private Integer percentOfVerified;
}
