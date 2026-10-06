package pl.psnc.vtl.portal.controllers.transcriptions;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.dto.transcriptions.PaginatedTranscriptions;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionExportAcceptInfo;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionExportDto;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionExportService;

import javax.validation.constraints.Min;

@RestController
@RequestMapping("/api")
@Validated
@RequiredArgsConstructor
@Tag(name = "TranscriptionExport")
public class TranscriptionExportController {

    private final TranscriptionExportService transcriptionExportService;

    @Value("${vtl.pagination.transcriptions.default-perpage}")
    private Integer transcriptionsDefualtPerpage;
    @Value("${vtl.pagination.transcriptions.maximal-perpage}")
    private Integer transcriptionsMaximalPerpage;

    @Operation(summary = "Mark transcription as ready for export to source domain")
    @PutMapping(path = "/transcriptions/{transcriptionId}/export/ready")
    public ResponseEntity<Void> markTranscriptionReadyForExport(@PathVariable long transcriptionId) {
        transcriptionExportService.markTranscriptionReadyForExport(transcriptionId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Mark transcription as accepted or rejected for export to source domain. When rejecting user can leave message for project coordinator")
    @PutMapping(path = "/transcriptions/{transcriptionId}/export/accept")
    public ResponseEntity<Void> markTranscriptionAcceptedForExport(@PathVariable long transcriptionId,
                                                                   @RequestBody TranscriptionExportAcceptInfo transcriptionExportAcceptInfo) {
        transcriptionExportService.markTranscriptionAcceptedForExport(transcriptionId, transcriptionExportAcceptInfo);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get list of all transcriptions that are ready for export.")
    @GetMapping(path = "/transcriptions/export")
    public ResponseEntity<PaginatedTranscriptions<TranscriptionExportDto>> getTranscriptionsReadyForExport(
            @RequestParam(required = false, name = "order-dir") Sort.Direction orderDir,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) Integer perpage) {
        perpage = getPagesDefaultPerpage(perpage, transcriptionsDefualtPerpage, transcriptionsMaximalPerpage);
        return ResponseEntity.ok(transcriptionExportService.getTranscriptionsReadyForExport(page, perpage, orderDir));
    }

    private Integer getPagesDefaultPerpage(Integer perpage, Integer defualtPerpage, Integer maximalPerpage) {
        perpage = (perpage == null) ? defualtPerpage : perpage;
        if (perpage > maximalPerpage) {
            throw new PageTooLargeException(maximalPerpage);
        }
        return perpage;
    }
}
