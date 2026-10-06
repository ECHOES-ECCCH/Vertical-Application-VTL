package pl.psnc.vtl.portal.controllers.layers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.model.layers.PageLayer.Id;
import pl.psnc.vtl.portal.services.progress.ProgressService;
import pl.psnc.vtl.portal.services.verses.UndoRedoService;

@RestController
@RequestMapping("/api/pageLayers/layer/{layerId}/page/{pageId}")
@RequiredArgsConstructor
@Tag(name = "UndoRedo", description = "Undo and redo current session's changes in a page/layer")
public class UndoRedoController {

    private final UndoRedoService undoRedoService;
    private final ProgressService progressService;

    @GetMapping(path = "/undo-redo", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Check if it's possible undo and redo at the moment")
    public ResponseEntity<UndoInfo> getUndoRedoInfo(@PathVariable long layerId, @PathVariable long pageId) {
        UndoInfo info = new UndoInfo();
        info.canUndo = undoRedoService.canUndo(new Id(pageId, layerId));
        info.canRedo = undoRedoService.canRedo(new Id(pageId, layerId));
        return ResponseEntity.ok(info);
    }

    @PutMapping(path = "/undo")
    public ResponseEntity<Void> undo(@PathVariable long layerId, @PathVariable long pageId) {
        undoRedoService.undo(new Id(pageId, layerId));
        progressService.updatePageProgress(pageId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = "/redo")
    public ResponseEntity<Void> redo(@PathVariable long layerId, @PathVariable long pageId) {
        undoRedoService.redo(new Id(pageId, layerId));
        progressService.updatePageProgress(pageId);
        return ResponseEntity.noContent().build();
    }

    public static class UndoInfo {
        public boolean canUndo;
        public boolean canRedo;
    }
}
