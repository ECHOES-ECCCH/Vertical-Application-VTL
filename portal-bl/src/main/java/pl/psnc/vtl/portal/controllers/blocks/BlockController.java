package pl.psnc.vtl.portal.controllers.blocks;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.dto.blocks.BlockDto;
import pl.psnc.vtl.portal.dto.blocks.MultipageBlockRemoveMode;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.services.blocks.BlockService;

import javax.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/transcriptions/{transcriptionId}/pages/{pageId}/layers/{layerId}/blocks")
@Validated
@RequiredArgsConstructor
@Tag(name = "Blocks", description = "Blocks of verses for maintaining hierarchical structures")
public class BlockController {

    private final BlockService blockService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Object>> addBlock(@PathVariable long transcriptionId, @PathVariable long pageId,
            @PathVariable long layerId, @RequestBody @Valid BlockDto block) {
        PageLayer.Id pageLayerId = new PageLayer.Id(pageId, layerId);
        blockService.addBlock(transcriptionId, pageLayerId, block);
        return ResponseEntity.ok(blockService.getBlocks(pageLayerId));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Object>> getBlocks(@PathVariable long transcriptionId, @PathVariable long pageId,
            @PathVariable long layerId) {
        return ResponseEntity.ok(blockService.getBlocks(transcriptionId, new PageLayer.Id(pageId, layerId)));
    }

    @DeleteMapping(path = "/{blockId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Object>> removeBlock(@PathVariable long transcriptionId, @PathVariable long pageId,
            @PathVariable long layerId, @PathVariable long blockId,
            @RequestParam(required = false) MultipageBlockRemoveMode multipageMode) {
        PageLayer.Id pageLayerId = new PageLayer.Id(pageId, layerId);
        blockService.removeBlock(transcriptionId, pageLayerId, blockId, multipageMode);
        return ResponseEntity.ok(blockService.getBlocks(pageLayerId));
    }

    @PutMapping(path = "/{blockId}/continuation", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Object>> changeBlockContinuation(@PathVariable long transcriptionId,
            @PathVariable long pageId, @PathVariable long layerId, @PathVariable long blockId) {
        PageLayer.Id pageLayerId = new PageLayer.Id(pageId, layerId);
        blockService.changeBlockContinuation(transcriptionId, pageLayerId, blockId);
        return ResponseEntity.ok(blockService.getBlocks(pageLayerId));
    }

    @DeleteMapping(path = "/{blockId}/continuation-multipage", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Object>> removeContinuationMultipage(@PathVariable long transcriptionId,
            @PathVariable long pageId, @PathVariable long layerId, @PathVariable long blockId) {
        PageLayer.Id pageLayerId = new PageLayer.Id(pageId, layerId);
        blockService.removeContinuationMultipage(transcriptionId, pageLayerId, blockId);
        return ResponseEntity.ok(blockService.getBlocks(pageLayerId));
    }

    @PostMapping(path = "/{blockId}/multipage", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Object>> addMultipageBlock(@PathVariable long transcriptionId,
            @PathVariable long pageId, @PathVariable long layerId, @PathVariable long blockId,
            @RequestParam int fromPage, @RequestParam int toPage) {
        PageLayer.Id pageLayerId = new PageLayer.Id(pageId, layerId);
        blockService.addMultipageBlock(transcriptionId, pageLayerId, blockId, fromPage, toPage);
        return ResponseEntity.ok(blockService.getBlocks(pageLayerId));
    }
}
