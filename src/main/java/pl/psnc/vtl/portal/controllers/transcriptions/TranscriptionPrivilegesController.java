package pl.psnc.vtl.portal.controllers.transcriptions;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionUserCore;
import pl.psnc.vtl.portal.dto.users.GroupIdCore;
import pl.psnc.vtl.portal.dto.users.UserEmail;
import pl.psnc.vtl.portal.model.permissions.TranscriptionGroup;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUser;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionPrivilegesService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@Tag(name = "Transcription", description = "Manage permissions for transcription")
@RequiredArgsConstructor
public class TranscriptionPrivilegesController {

    private final TranscriptionPrivilegesService transcriptionPrivilegesService;


    @PostMapping(path = "/transcriptions/{transcriptionId}/allowed-users", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TranscriptionUser>> addUserToTranscription(
            @RequestBody UserEmail email,
            @PathVariable("transcriptionId") long transcriptionId,
            @RequestHeader("Origin") String origin) {
        List<TranscriptionUser> transcriptionUsers = transcriptionPrivilegesService
                .addUserToTranscription(email.getEmail(), transcriptionId, origin);
        return ResponseEntity.ok(transcriptionUsers);
    }

    @GetMapping(path = "/transcriptions/{transcriptionId}/allowed-users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TranscriptionUser>> getUserTranscription(
            @PathVariable("transcriptionId") long transcriptionId) {
        List<TranscriptionUser> transcriptionUsers =
                transcriptionPrivilegesService.getUserTranscription(transcriptionId);
        return ResponseEntity.ok(transcriptionUsers);
    }


    @PutMapping(path = "/transcriptions/{transcriptionId}/allowed-users", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TranscriptionUser>> updateUserRoleInTranscription(
            @RequestBody TranscriptionUserCore transcriptionUserId,
            @PathVariable("transcriptionId") long transcriptionId,
            @RequestHeader("Origin") String origin) {
        List<TranscriptionUser> transcriptionUsers = transcriptionPrivilegesService
                .updateUserRoleInTranscription(transcriptionUserId, transcriptionId, origin);
        return ResponseEntity.ok(transcriptionUsers);
    }


    @DeleteMapping(path = "/transcriptions/{transcriptionId}/allowed-users/user/{userId}")
    public ResponseEntity<List<TranscriptionUser>> deleteUserFromTranscription(@PathVariable Long userId,
                                                                               @PathVariable("transcriptionId") long transcriptionId) {
        List<TranscriptionUser> transcriptionUser = transcriptionPrivilegesService
                .deleteUserFromTranscription(userId, transcriptionId);
        return ResponseEntity.ok(transcriptionUser);
    }


    @PostMapping(path = "/transcriptions/{transcriptionId}/allowed-groups", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TranscriptionGroup>> addGroupToTranscription(@RequestBody GroupIdCore groupIdCore,
                                                                            @PathVariable("transcriptionId") long transcriptionId) {
        List<TranscriptionGroup> transcriptionUser = transcriptionPrivilegesService
                .addGroupToTranscription(groupIdCore, transcriptionId);
        return ResponseEntity.ok(transcriptionUser);
    }


    @DeleteMapping(path = "/transcriptions/{transcriptionId}/allowed-groups/{groupId}")
    public ResponseEntity<List<TranscriptionGroup>> deleteGroupFromTranscription(@PathVariable("groupId") long groupId,
                                                                                 @PathVariable("transcriptionId") long transcriptionId) {
        List<TranscriptionGroup> transcriptionGroup = transcriptionPrivilegesService
                .deleteGroupFromTranscription(groupId, transcriptionId);
        return ResponseEntity.ok(transcriptionGroup);
    }

}
