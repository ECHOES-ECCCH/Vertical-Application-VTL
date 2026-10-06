package pl.psnc.vtl.portal.dto.transcriptions.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.verses.projections.VersesStatsForLayer;

/**
 * Layer statistics dto object.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LayerStatisticsDto {

    /**
     * Layer id.
     */
    private long layerId;

    /**
     * Number of transcribed verses.
     */
    private long versesTranscribed;

    /**
     * Number of all verses.
     */
    private long allVerses;

    /**
     * Number of verified verses.
     */
    private long versesMarkedAsVerified;

    /**
     * Number of incorrect verses.
     */
    private long versesMarkedAsIncorrect;

    public static LayerStatisticsDto of(VersesStatsForLayer lsp) {
        return LayerStatisticsDto.builder().layerId(lsp.getLayerId())
                .versesTranscribed(lsp.getTranscribedCount()).allVerses(lsp.getAllCount())
                .versesMarkedAsVerified(lsp.getVerifiedCount()).versesMarkedAsIncorrect(lsp.getIncorrectCount())
                .build();
    }
}
