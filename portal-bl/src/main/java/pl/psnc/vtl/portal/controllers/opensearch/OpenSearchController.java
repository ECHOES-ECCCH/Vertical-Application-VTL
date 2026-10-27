package pl.psnc.vtl.portal.controllers.opensearch;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.vtl.portal.dto.transcriptions.*;
import pl.psnc.vtl.portal.services.opensearch.OpenSearchService;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionListService;
import pl.psnc.vtl.portal.utils.search.OpenSearchConverterData;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OpenSearchController {

    private final OpenSearchService openSearchService;

    private final TranscriptionListService transcriptionListService;

    @GetMapping(value = "/opensearch", produces = "application/xml;charset=UTF-8")
    @Operation(summary = "Returns opensearch results")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of search results", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Unsupported language", content = @Content(schema = @Schema(implementation = String.class)))})
    public ResponseEntity<String> opensearch(
            @Parameter(description = "contains content of query, in case of advance search is built using AND and OR conjunctions", required = true) @RequestParam(value = "searchTerms") String searchTerms,
            @Parameter(description = "current page", required = true) @RequestParam(value = "startPage") int startPage,
            @Parameter(description = "current page size", required = true) @RequestParam(value = "count") int count,
            @Parameter(description = "current language", required = true) @RequestParam(value = "language", defaultValue = "pl-PL") String language) {

        if (!language.equals("pl-PL")) {
            return new ResponseEntity<>("There are not allowed any languages different from pl-PL.", HttpStatus.BAD_REQUEST);
        }

        PaginatedTranscriptions<TranscriptionSearchResult> searchResult = transcriptionListService.searchTranscriptions(
                searchTerms,
                SearchType.ALL,
                null,
                SearchScope.ALL,
                SearchVisibility.PUBLIC,
                SearchOrder.HIT_COUNT,
                startPage,
                count
        );

        OpenSearchConverterData params = new OpenSearchConverterData(searchResult, startPage, count, searchTerms);
        return new ResponseEntity<>(openSearchService.getDocument(params), HttpStatus.OK);
    }

    @GetMapping(value = "/opensearch/description.xml", produces = "application/xml;charset=UTF-8")
    @Operation(summary = "Returns detailed OpenSearch description document")
    public ResponseEntity<String> description() {
        return new ResponseEntity<>(openSearchService.getDescription(), HttpStatus.OK);
    }

}
