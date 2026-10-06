package pl.psnc.vtl.portal.controllers.layers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.vtl.portal.dto.layers.PageLayerDto;
import pl.psnc.vtl.portal.services.layers.PageLayerService;

import java.util.List;

@RestController
@RequestMapping("/api/pageLayers")
@RequiredArgsConstructor
public class PageLayerController {

    private final PageLayerService pageLayerService;

    @Operation(summary = "Get page layers for given layerId")
    @GetMapping(path = "/layer/{layerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PageLayerDto>> getAllPageLayersPerLayer(@PathVariable("layerId") Long layerId) {
        return ResponseEntity.ok(pageLayerService.getAllPageLayersPerLayer(layerId));
    }

    @Operation(summary = "Get page layer for given layerId and pageId")
    @GetMapping(path = "/layer/{layerId}/page/{pageId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageLayerDto> getPageLayer(@PathVariable("layerId") Long layerId,
                                                     @PathVariable("pageId") Long pageId) {
        return ResponseEntity.ok(pageLayerService.returnPageLayer(layerId, pageId));
    }


    @Operation(summary = "Get page layers for given pageId")
    @GetMapping(path = "/page/{pageId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PageLayerDto>> getPageLayersPerPage(@PathVariable("pageId") Long pageId) {
        return ResponseEntity.ok(pageLayerService.getAllPageLayersPerPage(pageId));
    }
}
