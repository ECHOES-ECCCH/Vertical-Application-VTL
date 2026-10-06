package pl.psnc.vtl.portal.controllers.iiifpresentation;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.services.iiifpresentation.IIIFPresentation2Service;
import pl.psnc.vtl.portal.services.iiifpresentation.IIIFPresentation3Service;

@RestController
@RequestMapping("/api")
@Tag(name = "IIIFPresentation")
@RequiredArgsConstructor
public class IIIFPresentationController {

    private final IIIFPresentation3Service iiifPresentation3Service;

    private final IIIFPresentation2Service iiifPresentation2Service;

    @GetMapping(value = "/iiif/transcriptions/{transcriptionId}/manifest.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTranscriptionIIIF(
            @PathVariable("transcriptionId") long transcriptionId,
            @RequestParam(value = "v", required = false) Integer v,
            @RequestParam(value = "layerId", required = false) Long layerId
    ) {

        if (v != null && v == 3) {
            return ResponseEntity.ok(iiifPresentation3Service.getTranscriptionManifest(transcriptionId, layerId));
        } else {
            return ResponseEntity.ok(iiifPresentation2Service.getTranscriptionManifest(transcriptionId, layerId));
        }
    }

    @GetMapping(value = "/iiif/transcriptions/{transcriptionId}/{pageId}/{layerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAnnotationList(
            @PathVariable("transcriptionId") long transcriptionId,
            @PathVariable("pageId") long pageId,
            @PathVariable("layerId") long layerId
    ) {
        return ResponseEntity.ok(iiifPresentation2Service.getAnnotationList(transcriptionId, pageId, layerId));
    }

}
