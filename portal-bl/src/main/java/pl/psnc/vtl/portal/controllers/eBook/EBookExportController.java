package pl.psnc.vtl.portal.controllers.eBook;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.services.eBook.AsyncTaskResponse;
import pl.psnc.vtl.portal.services.eBook.EBookService;
import pl.psnc.vtl.portal.services.eBook.Ebook;
import pl.psnc.vtl.portal.services.eBook.EbookDto;

import java.net.URI;

@RestController
@RequestMapping("/api/transcriptions")
@RequiredArgsConstructor
@Tag(name = "Ebooks")
class EBookExportController {

    private final EBookService eBookService;

    @Operation(summary = "method return status of export process with status field corresponding to actual status and details;" +
            "details containing file location for done status , " +
            "details for failed export " +
            "and null for export in progress")
    @GetMapping("/eBooks/{resourceHash}")
    ResponseEntity<AsyncTaskResponse> getExportedEBook(@PathVariable("resourceHash") String resourceHash) {

        return ResponseEntity.ok(eBookService.getEBookLocation(resourceHash));
    }


    @Operation(summary = "Method to export single layer")
    @PostMapping(path = "/{transcriptionId}/eBooks/{layerId}")
    ResponseEntity<Void> addExportTaskWithLayer(
            @PathVariable long transcriptionId,
            @PathVariable long layerId,
            @RequestParam("format") Ebook format,
            @RequestParam(value = "titlepage", required = false) Boolean titlePage) {

        boolean createTitlePage = titlePage != null ? titlePage : true;

        return ResponseEntity.created(URI.create(
                "/api/transcriptions/eBooks/" + eBookService.addExportProcess(
                        new EbookDto(format, transcriptionId, layerId, createTitlePage, null))
        )).build();
    }
}
