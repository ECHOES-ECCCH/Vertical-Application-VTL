package pl.psnc.vtl.iiif.controllers.image;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.iiif.converters.ConversionException;
import pl.psnc.vtl.iiif.model.DownloadFilesResponse;
import pl.psnc.vtl.iiif.model.RemoveFileResponse;
import pl.psnc.vtl.iiif.services.image.IIIFServerService;
import pl.psnc.vtl.iiif.services.image.NameCannotBeExtractedException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/iiif")
@RequiredArgsConstructor
public class IIIFServerController {

    private final IIIFServerService iiifServerService;


    @PostMapping(path = "/{transcriptionId}/save-image/{filename}")
    @Operation(summary = "Save image file dor specified transcription.")
    public void saveImage(@PathVariable("transcriptionId") long transcriptionId,
            @PathVariable("filename") String filename, HttpServletRequest request)
            throws IOException {
        iiifServerService.saveImage(transcriptionId, request.getInputStream(), filename);
    }


    @PutMapping(path = "/{transcriptionId}/make-pyramid/{filename}")
    @Operation(summary = "Create pyramid file from specified file from specified transcription.")
    public ResponseEntity<List<String>> makePyramid(@PathVariable("transcriptionId") long transcriptionId,
            @PathVariable("filename") String filename)
            throws ConversionException {
        List<String> response = iiifServerService.makePyramid(transcriptionId, filename);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping(path = "/{transcriptionId}")
    @Operation(summary = "Remove all files related to specified transcription.")
    public ResponseEntity<RemoveFileResponse> deleteTranscription(@PathVariable("transcriptionId") long transcriptionId)
            throws IOException {
        return ResponseEntity.ok(iiifServerService.deleteTranscriptionImages(transcriptionId));
    }


    @DeleteMapping(path = "/clear-all")
    @Operation(summary = "Remove all files from the server.")
    public void deleteTranscription()
            throws IOException {
        iiifServerService.deleteAllTranscriptionsImages();
    }


    @PostMapping(path = "/{transcriptionId}/download-images")
    @Operation(summary = "Download all images from given urls and saves them for specified transcription.")
    public ResponseEntity<DownloadFilesResponse> downloadImages(@PathVariable("transcriptionId") long transcriptionId,
            @RequestBody List<String> urls)
            throws IOException, NameCannotBeExtractedException {
        return ResponseEntity.ok(iiifServerService.downloadImages(transcriptionId, urls));
    }


    @DeleteMapping(path = "/{transcriptionId}/delete-image/{filename}")
    @Operation(summary = "Delete specific image from specific transcription.")
    public ResponseEntity<RemoveFileResponse> deleteImage(@PathVariable("transcriptionId") long transcriptionId,
            @PathVariable("filename") String filename)
            throws IOException {
        return ResponseEntity.ok(iiifServerService.deleteImage(transcriptionId, filename));
    }

    @DeleteMapping(path = "/{transcriptionId}/delete-pyramid/{filename}")
    @Operation(summary = "Delete specific pyramid from specific transcription.")
    public ResponseEntity<Void> deletePyramid(@PathVariable("transcriptionId") long transcriptionId,
            @PathVariable("filename") String filename)
            throws IOException {
        iiifServerService.deletePyramid(transcriptionId, filename);
        return ResponseEntity.ok().build();
    }

}
