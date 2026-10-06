package pl.psnc.vtl.portal.controllers.indexes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.dto.indexes.IndexCore;
import pl.psnc.vtl.portal.dto.indexes.IndexDto;
import pl.psnc.vtl.portal.services.indexes.IndexService;

@RestController
@RequestMapping("/api/indexes")
@RequiredArgsConstructor
@Tag(name = "Indexes", description = "All operations related to indexes that additionally describes the content of the verse. ")
public class IndexController {

    private final IndexService indexService;

    @Operation(summary = " Get index with given id")
    @GetMapping(path = "/{indexId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IndexDto> getIndex(@PathVariable("indexId") Long indexId) {
        return ResponseEntity.ok(indexService.getIndex(indexId));
    }

    @Operation(summary = "Update index")
    @PutMapping(path = "/{indexId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IndexDto> updateIndex(@PathVariable("indexId") Long indexId,
                                                @RequestBody IndexCore indexCore) {
        return ResponseEntity.ok(indexService.updateIndex(indexId, indexCore));
    }

    @Operation(summary = "Delete index with given id")
    @DeleteMapping(path = "/{indexId}")
    public void deleteIndex(@PathVariable("indexId") Long indexId) {
        indexService.deleteIndex(indexId);
    }

    @Operation(summary = "Add index to verse")
    @PutMapping(path = "/{indexId}/verses/{verseId}")
    public ResponseEntity<IndexDto> addIndexToVerse(@PathVariable Long indexId, @PathVariable Long verseId) {
        return ResponseEntity.ok(indexService.addIndexToVerse(indexId, verseId));
    }

    @Operation(summary = "Add index to word")
    @PutMapping(path = "/{indexId}/words/{wordId}")
    public ResponseEntity<IndexDto> addIndexToWord(@PathVariable Long indexId, @PathVariable Long wordId) {
        return ResponseEntity.ok(indexService.addIndexToWord(indexId, wordId));
    }

    @Operation(summary = "Remove index from verse")
    @DeleteMapping(path = "/{indexId}/verses/{verseId}")
    public ResponseEntity<Void> removeIndexFromVerse(@PathVariable Long indexId, @PathVariable Long verseId) {
        indexService.removeIndexFromVerse(indexId, verseId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Remove index from word")
    @DeleteMapping(path = "/{indexId}/words/{wordId}")
    public ResponseEntity<Void> removeIndexFromWord(@PathVariable Long indexId, @PathVariable Long wordId) {
        indexService.removeIndexFromWord(indexId, wordId);
        return ResponseEntity.ok().build();
    }
}
