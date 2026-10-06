package pl.psnc.vtl.portal.controllers.iiifserver;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.vtl.portal.services.iiifserver.IIIFServerService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class IIIFServerController {

    private final IIIFServerService iiifServerService;


    @Operation(summary = "Request IIIF manifest for the given page", tags = "IIIF")
    @GetMapping(path = "/iiif/pages/{pageId}/info.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public String infoJsonProvider(@PathVariable("pageId") long pageId) {
        return iiifServerService.infoJsonProvider(pageId);
    }
}
