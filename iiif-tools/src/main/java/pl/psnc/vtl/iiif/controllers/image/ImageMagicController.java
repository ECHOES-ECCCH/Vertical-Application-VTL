package pl.psnc.vtl.iiif.controllers.image;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.iiif.services.image.ImageMagicService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageMagicController {

    private final ImageMagicService imageMagicService;

    @GetMapping(path = "iiif/{transcriptionId}/{filename}/pages")
    public ResponseEntity<Integer> getPages(@PathVariable("transcriptionId") long transcriptionId,
            @PathVariable("filename") String filename)
            throws Exception {
        Integer response = imageMagicService.getPagesNumber(transcriptionId, filename);
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "iiif/{transcriptionId}/pages", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Integer>> getPages(@PathVariable("transcriptionId") long transcriptionId, @RequestBody List<String> filesNames) {
        Map<String, Integer> filesNumberOfPages = new HashMap<>();
        filesNumberOfPages = filesNames.stream().collect(
                Collectors.toMap(fileName -> fileName, fileName -> {
                    try {
                        return imageMagicService.getPagesNumber(transcriptionId, fileName);
                    } catch (IOException ignore) {}
                    return -1;
                }));

        return ResponseEntity.ok(filesNumberOfPages);
    }
}
