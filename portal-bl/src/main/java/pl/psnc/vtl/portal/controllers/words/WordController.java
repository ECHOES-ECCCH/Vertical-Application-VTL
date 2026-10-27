package pl.psnc.vtl.portal.controllers.words;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.dto.verses.VersesAndBlocksDto;
import pl.psnc.vtl.portal.dto.word.TeiElementDto;
import pl.psnc.vtl.portal.model.words.Word;
import pl.psnc.vtl.portal.services.verses.VerseService;
import pl.psnc.vtl.portal.services.words.WordService;

import javax.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
@RequiredArgsConstructor
@Tag(name = "Words", description = "Words are optional building blocks of verses")
public class WordController {

    private final WordService wordService;
    private final VerseService verseService;

    @PostMapping(path = "/verses/{verseId}/words", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Creates words and returns current list of all words in the verse")
    public ResponseEntity<VersesAndBlocksDto> createWords(@PathVariable long verseId, @RequestBody List<Word> words) {
        VersesAndBlocksDto result = verseService.getVersesAndBlocksDto(wordService.createWords(verseId, words));
        return ResponseEntity.ok(result);
    }

    @PutMapping(path = "/verses/{verseId}/words", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Updates words and returns current list of all words in the verse. "
            + "Throws error if any of the words does not belong to the verse")
    public ResponseEntity<VersesAndBlocksDto> updateWords(@PathVariable long verseId, @RequestBody List<Word> words) {
        VersesAndBlocksDto result = verseService.getVersesAndBlocksDto(wordService.updateWords(verseId, words));
        return ResponseEntity.ok(result);
    }

    @DeleteMapping(path = "/verses/{verseId}/words", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Deletes words and returns current list of all words in the verse. "
            + "Throws error if any of the words does not belong to the verse")
    public ResponseEntity<VersesAndBlocksDto> deleteWords(@PathVariable long verseId, @RequestBody List<Long> wordIds) {
        VersesAndBlocksDto result = verseService.getVersesAndBlocksDto(wordService.deleteWords(verseId, wordIds));
        return ResponseEntity.ok(result);
    }

    @DeleteMapping(path = "/words/{wordId}/gap")
    @Operation(description = "Removes TEI gap marker from word")
    public VersesAndBlocksDto removeTeiGapMarker(@PathVariable long wordId) {
        return verseService.getVersesAndBlocksDto(wordService.removeTeiGapMarker(wordId));
    }

    @PutMapping(path = "/words/tei_elements")
    @Operation(description = "Adds specified TEI element to the specified words")
    public VersesAndBlocksDto addTeiElementToWords(@Valid @RequestBody TeiElementDto teiElementDto) {
        return verseService.getVersesAndBlocksDto(wordService.addTeiElementToWords(teiElementDto));
    }

    @DeleteMapping(path = "/words/{wordId}/tei_add")
    @Operation(description = "Removes TEI add element to the word")
    public VersesAndBlocksDto removeTeiAddMarker(@PathVariable long wordId) {
        return verseService.getVersesAndBlocksDto(wordService.removeTeiAddMarker(wordId));
    }

    @DeleteMapping(path = "/words/{wordId}/tei_del")
    @Operation(description = "Removes TEI del element from the word")
    public VersesAndBlocksDto removeTeiDelMarker(@PathVariable long wordId) {
        return verseService.getVersesAndBlocksDto(wordService.removeTeiDelMarker(wordId));
    }

    @DeleteMapping(path = "/words/{wordId}/tei_subst")
    @Operation(description = "Removes TEI subst element from the word")
    public VersesAndBlocksDto removeTeiSubstMarker(@PathVariable long wordId) {
        return verseService.getVersesAndBlocksDto(wordService.removeTeiSubstMarker(wordId));
    }
}
