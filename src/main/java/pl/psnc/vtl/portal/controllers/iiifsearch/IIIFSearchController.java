package pl.psnc.vtl.portal.controllers.iiifsearch;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.services.iiifsearch.IIIFSearchService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "IIIFSearch")
@Slf4j
public class IIIFSearchController {

    private final IIIFSearchService searchService;

    @GetMapping(path = "/iiif/transcriptions/{transcriptionId}/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> searchTranscriptions(
            @PathVariable("transcriptionId") Long transcriptionId,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "layerId", required = false) Long layerId
    ) {
        //FIXME empty q is allowed by iiif
        return ResponseEntity.ok(searchService.getSearchResults(transcriptionId, q, layerId));
    }
}
