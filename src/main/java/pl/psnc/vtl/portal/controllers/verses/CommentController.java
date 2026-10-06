package pl.psnc.vtl.portal.controllers.verses;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.dto.verses.CommentCore;
import pl.psnc.vtl.portal.model.verses.Comment;
import pl.psnc.vtl.portal.services.verses.CommentService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Retrieve, add, modify, delete comments for verses")
public class CommentController {

    private final CommentService commentService;


    @GetMapping(path = "/comments/{verseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Comment>> getComments(@PathVariable("verseId") long verseId) {
        List<Comment> comments = commentService.getComments(verseId);
        return ResponseEntity.ok(comments);
    }


    @PostMapping(path = "/comments/{verseId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> addComment(@PathVariable("verseId") Long verseId, @RequestBody CommentCore comment) {
        Integer count = commentService.addComment(verseId, comment);
        return ResponseEntity.ok(count);
    }


    @PutMapping(path = "/comments/{commentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateComment(@PathVariable("commentId") Long commentId, @RequestBody CommentCore comment) {
        commentService.updateComment(commentId, comment);
    }


    @DeleteMapping("/comments/{commentId}")
    public void deleteComment(@PathVariable("commentId") long commentId) {
        commentService.deleteComment(commentId);
    }

}
