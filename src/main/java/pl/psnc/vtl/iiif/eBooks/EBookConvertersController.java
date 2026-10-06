package pl.psnc.vtl.iiif.eBooks;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class EBookConvertersController {

    private final EBookservice exportService;

    @PostMapping(path = "/transcriptions/{Id}/eBooks", consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<Void> exportEBook(@PathVariable("Id") Long transcriptionId,
                                     @RequestParam("format") EBookFormat eBookFormat,
                                     @RequestParam("hash") String hash,
                                     @RequestParam("title") String title,
                                     @RequestParam("author") String author,
                                     @RequestParam(required = false, value = "mapping") String json,
                                     @RequestParam(value = "titlepage", required = false) String titlePage,
                                     @RequestBody @RequestParam("files") MultipartFile[] files) {
        exportService.exportAndGetLocation(eBookFormat, files, transcriptionId, title, author, hash, json, titlePage);
        return ResponseEntity.created(null).build();
    }

    @GetMapping(path = "/transcriptions/{Id}/eBooks", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<EBookservice.ExportedEBook> exportEBook(@PathVariable("Id") Long transcriptionId,
                                                           @RequestParam("format") EBookFormat eBookFormat,
                                                           @RequestParam("hash") String hash) {
        return ResponseEntity.ok(exportService.getExportedEBook(transcriptionId, eBookFormat, hash));
    }


}
