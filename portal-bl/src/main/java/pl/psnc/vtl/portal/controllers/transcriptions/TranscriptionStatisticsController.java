package pl.psnc.vtl.portal.controllers.transcriptions;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.dto.transcriptions.statistics.TranscriptionStatistics;
import pl.psnc.vtl.portal.repositories.verses.StatsSortField;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionStatisticsService;
import pl.psnc.vtl.portal.services.verses.TranscriptionVerseStatisticsService;
import pl.psnc.vtl.portal.services.verses.TranscriptionVerseUserStatistic;
import pl.psnc.vtl.portal.utils.PageRequestUtils;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Transcription statistics")
public class TranscriptionStatisticsController {

    private final TranscriptionStatisticsService transcriptionStatisticsService;

    private final TranscriptionVerseStatisticsService transcriptionVerseStatisticsService;

    private final PageRequestUtils pageRequestUtils;

    @Operation(summary = "Get transcription statistics for given transcription id according to the specified sort field and order")
    @GetMapping(path = "/transcriptions/{transcriptionId}/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TranscriptionStatistics> getTranscriptionStatistics(
            @PathVariable long transcriptionId,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "perPage", required = false, defaultValue = "10") Integer perPage,
            @Parameter(name = "sortBy", description = "Field of the page's statistics to sort a list of transcription's " +
                    "page statistics", content = @Content(schema = @Schema(type = "string",
                    allowableValues = {"page-no", "in-transcription", "accepted" , "rejected", "to-accept"}, defaultValue = "page-no")))
            @RequestParam(required = false, defaultValue = "page-no") StatsSortField sortBy,
            @Parameter(name = "sortDirection", description = "Direction of the sort",
                    content = @Content(schema = @Schema(type = "string", allowableValues = {"asc", "desc"}, defaultValue = "asc")))
            @RequestParam(required = false, defaultValue = "asc") Sort.Direction sortDirection) {
        // manipulate page param because API expects to count pages starting from 1, while JPA repository counts pages
        // starting from 0
        PageRequest pageable = pageRequestUtils.getLimitedPageRequest(page - 1, perPage, sortBy, sortDirection);
        TranscriptionStatistics stats = transcriptionStatisticsService.getTranscriptionStatistics(transcriptionId, pageable);
        // manipulate page param because API expects to count pages starting from 1, while JPA repository counts pages
        // starting from 0
        stats.getPaginatedPagesStatistics().forEach(ps -> ps.setPage(ps.getPage() + 1));
        return ResponseEntity.ok(stats);
    }


    @Operation(summary = "Get transcription verse statistics for given transcription id")
    @GetMapping(path = "/transcriptions/{transcriptionId}/verse-statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TranscriptionVerseUserStatistic>> getTranscriptionStatistics(@PathVariable long transcriptionId) {
        return ResponseEntity.ok(transcriptionVerseStatisticsService.getTranscriptionVerseUserStatistics(transcriptionId));
    }

}
