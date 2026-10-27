package pl.psnc.vtl.portal.dto.transcriptions.statistics;

import lombok.Data;
import pl.psnc.vtl.portal.dto.pages.PaginatedPagesStatistics;

import java.util.List;

/**
 * Transcription statistics object.
 */
@Data
public class TranscriptionStatistics {

    /**
     * Transcription title.
     */
    private String titleLabel;

    /**
     * Authors label.
     */
    private String authorsLabel;

    /**
     * Is transcription hidden.
     */
    private boolean hidden;

    /**
     * List of layer statistics.
     */
    private List<LayerStatisticsDto> layersStatistics;

    /**
     * Paginated list of page statistics.
     */
    private List<PaginatedPagesStatistics> paginatedPagesStatistics;

}
