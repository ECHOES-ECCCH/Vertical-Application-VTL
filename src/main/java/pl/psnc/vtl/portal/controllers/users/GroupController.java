package pl.psnc.vtl.portal.controllers.users;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.dto.users.GroupCore;
import pl.psnc.vtl.portal.dto.users.UserEmail;
import pl.psnc.vtl.portal.dto.users.UserGroupCore;
import pl.psnc.vtl.portal.model.users.Group;
import pl.psnc.vtl.portal.services.users.GroupService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name= "Groups", description = "retrieve, create, modify groups")
public class GroupController {

    private final GroupService groupService;

    @Value("${vtl.pagination.transcriptions.list-count}")
    private Integer defaultListCount;


    @GetMapping(path = "/groups/{groupId:[0-9]+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Group> getGroup(@PathVariable("groupId") Long groupId) {
        Group group = groupService.getGroup(groupId);
        return ResponseEntity.ok(group);
    }


    @PostMapping(path = "/groups", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Group> createGroup(@RequestBody GroupCore newGroup) {
        Group group = groupService.createGroup(newGroup);
        return ResponseEntity.ok(group);
    }


    @PutMapping(path = "/groups/{groupId:[0-9]+}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Group> updateGroupName(@PathVariable("groupId") Long groupId,
                                                 @RequestBody GroupCore newGroup) {
        Group group = groupService.updateGroup(groupId, newGroup);
        return ResponseEntity.ok(group);
    }


    @DeleteMapping("/groups/{groupId:[0-9]+}")
    public void deleteGroup(@PathVariable("groupId") Long groupId) {
        groupService.deleteGroup(groupId);
    }


    @PostMapping(path = "/users-groups/{groupId:[0-9]+}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Group> addUserToGroup(@PathVariable("groupId") Long groupId, @RequestBody UserEmail email) {
        Group group = groupService.addUserToGroup(groupId, email.getEmail());
        return ResponseEntity.ok(group);
    }


    @PutMapping(path = "/users-groups/{groupId:[0-9]+}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Group> changeUserRoleInGroup(@PathVariable("groupId") Long groupId,
                                                       @RequestBody UserGroupCore userGroup) {
        Group group = groupService.changeUserRoleInGroup(groupId, userGroup.getUser().getId(), userGroup.getRole());
        return ResponseEntity.ok(group);
    }


//    @DeleteMapping("/users-groups/{groupId}")
//    public ResponseEntity<Group> removeUserFromGroup(@PathVariable("groupId") Long groupId, @RequestBody UserId userId) {
//        Group group = groupService.removeUserFromGroup(groupId, userId.getId());
//        return ResponseEntity.ok(group);
//    }

    @DeleteMapping("/users-groups/{groupId:[0-9]+}/user/{userId:[0-9]+}")
    public ResponseEntity<Group> removeUserFromGroup(@PathVariable("groupId") Long groupId, @PathVariable("userId") Long userId) {
        Group group = groupService.removeUserFromGroup(groupId, userId);

        return ResponseEntity.ok(group);
    }

    @GetMapping(path = {"/groups/my-groups/{pattern}", "/groups/my-groups"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Group>> getAllowedGroups(@PathVariable(required = false) String pattern,
                                                        @RequestParam(required = false) Integer listCount) {

        if (listCount == null)
            listCount = defaultListCount;

        return ResponseEntity.ok(groupService.findUserGroupsMatchingPattern(pattern, listCount));
    }

}
