package pl.psnc.vtl.portal.controllers.transcriptions;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.services.transcriptions.UserLastTranscriptionService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Last transcriptions")
public class UserLastTranscriptionController {

    private final UserLastTranscriptionService userLastTranscriptionService;


    @GetMapping(path = "/my-last-transcription", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Transcription> getUserLastTranscription() {
        return ResponseEntity.ok(userLastTranscriptionService.getUserLastTranscription());
    }


    @GetMapping(path = "/last-transcriptions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Transcription>> getUserLastTranscriptions() {
        return ResponseEntity.ok(userLastTranscriptionService.getUserLastTranscriptions());
    }

}
