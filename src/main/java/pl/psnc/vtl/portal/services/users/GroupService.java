package pl.psnc.vtl.portal.services.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.dto.users.GroupCore;
import pl.psnc.vtl.portal.dto.users.UserGroupInline;
import pl.psnc.vtl.portal.dto.users.UserRoleId;
import pl.psnc.vtl.portal.model.users.Group;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.users.UserGroup;
import pl.psnc.vtl.portal.model.users.UserGroupId;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionGroupRepository;
import pl.psnc.vtl.portal.repositories.users.GroupRepository;
import pl.psnc.vtl.portal.repositories.users.UserGroupRepository;
import pl.psnc.vtl.portal.repositories.users.UserRepository;
import pl.psnc.vtl.portal.repositories.users.UserRoleRepository;
import pl.psnc.vtl.portal.services.permissions.GroupPermissionService;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;

import javax.persistence.EntityNotFoundException;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;

    private final UserGroupRepository userGroupRepository;

    private final UserRepository userRepository;

    private final UserRoleRepository userRoleRepository;

    private final TranscriptionGroupRepository transcriptionGroupRepository;

    private final GroupPermissionService groupPermissionService;

    private final UserService userService;

    private  final LoggedInUserHolder loggedInUserHolder;

    public Group getGroup(Long groupId) {
        Group userGroup = groupRepository.findById(groupId).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Group.class + " with id '" + groupId + "'!"));
        //first call db than chaeck access?
        groupPermissionService.checkAdminAccess(groupId);
        return completeGroup(userGroup);
    }


    private Group completeGroup(Group group) {
        List<UserGroup> userGroups = userGroupRepository.findByGroupIdOrderByCreationDateAsc(group.getId());
        List<UserGroupInline> members = userGroups.stream().map(userGroup -> {
            UserGroupInline.UserGroupInlineBuilder builder = UserGroupInline.builder();
            builder.user(userGroup.getUser());
            builder.role(userGroup.getRole());
            return builder.build();
        }).collect(Collectors.toList());
        group.setMembers(members);
        group.setOwner(group.getCreator().getId().equals(loggedInUserHolder.getLoggedInUser().getId()));
        return group;
    }


    private Group validateGroup(GroupCore newGroup, Long groupId, User user) {
        Group group = getOrCreateGroup(groupId);
        if (group.getCreator() == null) {
            if (groupRepository.existsByCreatorEmailAndName(user.getEmail(), newGroup.getName())) {
                throw new GroupAlreadyExistsException(
                        "User " + user.getEmail() + " is already owner of a group with name " + newGroup.getName(),
                        "groupAlreadyExists");
            }
            group.setCreator(user);
        } else {
            if (groupRepository.existsByCreatorEmailAndName(group.getCreator().getEmail(), newGroup.getName())) {
                throw new GroupAlreadyExistsException(
                        "User " + group.getCreator().getEmail() + " is already owner of a group with name " + newGroup
                                .getName(), "groupAlreadyExists");
            }
        }
        group.setName(newGroup.getName());
        return group;
    }


    private Group getOrCreateGroup(Long groupId) {
        if (groupId != null) {
            //getOne - LAZY loading FindOneByID - EAGER no need to set eager loading everywhere and call getONe
            return groupRepository.getOne(groupId);
        } else {
            return new Group();
        }
    }


    public Group createGroup(GroupCore newGroup) {
        User user = loggedInUserHolder.getLoggedInUser();
        Group group = validateGroup(newGroup, null, user);
        group = groupRepository.save(group);

        UserGroup userGroup = new UserGroup();
        userGroup.setGroup(group);
        userGroup.setUser(user);
        userGroup.setRole(userRoleRepository.getOne("admin"));
        userGroupRepository.save(userGroup);

        return completeGroup(group);
    }


    public Group updateGroup(Long groupId, GroupCore newGroup) {
        if (!groupRepository.existsById(groupId)) {
            throw new EntityNotFoundException("Unable to find " + Group.class + " with id '" + groupId + "'!");
        }
        groupPermissionService.checkOwnerAccess(groupId);
        Group group = validateGroup(newGroup, groupId, loggedInUserHolder.getLoggedInUser());
        group = groupRepository.save(group);
        return completeGroup(group);
    }


    public void deleteGroup(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new EntityNotFoundException("Unable to find " + Group.class + " with id '" + groupId + "'!");
        }
        groupPermissionService.checkOwnerAccess(groupId);

        transcriptionGroupRepository.deleteByGroupId(groupId);

        userGroupRepository.deleteByGroupId(groupId);
        groupRepository.deleteById(groupId);
    }


    private UserGroup validateUserGroup(Long groupId, String email) {
        if (userGroupRepository.existsByGroupIdAndUserEmail(groupId, email)) {
            throw new UserGroupAlreadyExsitsException("Given user already exists in the group",
                    "userAlreadyExistsInGroup");
        }

        UserGroup newUserGroup = new UserGroup();
        newUserGroup.setUser(userRepository.findByEmail(email).orElseThrow(
                () -> new NoUserWithGivenEmailException("User with email " + email + " does not exist", "noUserWithGivenEmail")));

        newUserGroup.setGroup(groupRepository.getOne(groupId));
        newUserGroup.setRole(userRoleRepository.getOne("contributor"));
        return newUserGroup;
    }


    public Group addUserToGroup(Long groupId, String email) {
        groupPermissionService.checkAdminAccess(groupId);
        UserGroup userGroup = validateUserGroup(groupId, email);

        userGroupRepository.save(userGroup);
        return completeGroup(userGroup.getGroup());
    }


    private UserGroup validateUserGroup(Long groupId, long userId, UserRoleId userRole) {
        UserGroup userGroup = userGroupRepository.findById(new UserGroupId(userId, groupId)).orElseThrow(
                () -> new EntityNotFoundException(
                        "Unable to find " + UserGroup.class + " with userId '" + userId + "' and groupId '" + groupId
                                + "'"));

        userGroup.setRole(userRoleRepository.getOne(userRole.getCode()));
        return userGroup;
    }


    public Group changeUserRoleInGroup(Long groupId, long userId, UserRoleId userRole) {
        groupPermissionService.checkAdminAccess(groupId);
        UserGroup userGroup = validateUserGroup(groupId, userId, userRole);
        userGroupRepository.save(userGroup);
        return completeGroup(userGroup.getGroup());
    }


    public Group removeUserFromGroup(Long groupId, long userId) {
        if (!groupRepository.existsById(groupId))
            throw new EntityNotFoundException("Unable to find " + Group.class + " with id '" + groupId + "'!");

        groupPermissionService.checkAdminAccess(groupId);

        User user = loggedInUserHolder.getLoggedInUser();
        if (userId == user.getId())
            throw new GroupCreatorRemovalException("You cannot delete yourself from group that you created!",
                    "cannotRemoveYourself");

        UserGroup userGroup = userGroupRepository.findById(new UserGroupId(userId, groupId)).orElseThrow(
                () -> new EntityNotFoundException(
                        "Unable to find " + UserGroup.class + " with userId '" + userId + "' and groupId '" + groupId
                                + "'"));

        userGroupRepository.delete(userGroup);

        return completeGroup(userGroup.getGroup());
    }

    @Transactional(readOnly = true)
    public List<Group> findUserGroupsMatchingPattern(String namePattern, int listCount) {
        List<Group> groupsList = userService.getMyProfile().getMyGroups();

        Predicate<Group> byName = group -> StringUtils.isBlank(namePattern)
                || group.getName().toLowerCase().contains(namePattern.toLowerCase());

        return groupsList.stream().filter(byName).limit(listCount).collect(Collectors.toList());
    }

}
