package pl.psnc.vtl.portal.controllers.imports;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.dto.imports.ImportTranscriptionDTO;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;
import pl.psnc.vtl.portal.services.imports.ImportTranscriptionService;

import java.net.URI;

@RestController
@RequestMapping("/api")
@Tag(name= "Import transcription", description = "create new transcription based on external resources")
@RequiredArgsConstructor
public class ImportsController {

    private final ImportTranscriptionService importsService;

    @Operation(summary = "Create transcription core from given resource Url and file Type, to initialize transcription import" )
    @PutMapping("/imports")
    public ResponseEntity<TranscriptionCore> importTranscriptionData(
            @RequestBody ImportTranscriptionDTO importTranscriptionDTO)
            throws ConfigurationException {
        return ResponseEntity.ok(importsService.importTranscriptionData(importTranscriptionDTO));
    }

    @Operation(summary = "Create transcription from given transcription core, returns response with location header what points to created resource location")
    @PostMapping("/imports")
    public ResponseEntity<Void> completeTranscriptionImport(@RequestBody TranscriptionCore transcriptionCore,
            @RequestHeader("Origin") String origin) {
        Long transcriptionId = importsService.createTranscription(transcriptionCore, origin).getId();
        return ResponseEntity.created(URI.create("/api/transcriptions/" + transcriptionId)).build();
    }

    @Operation(summary = "Updates metadata and redownload pages for given transcription")
    @PostMapping("/imports/{transcriptionId}/redownload")
    public ResponseEntity<Void> redownloadTranscription(@PathVariable Long transcriptionId) {
        importsService.redownloadTranscription(transcriptionId);
        return ResponseEntity.ok().build();
    }
}
