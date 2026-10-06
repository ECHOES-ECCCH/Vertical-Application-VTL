package pl.psnc.vtl.portal.services.permissions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.model.users.*;
import pl.psnc.vtl.portal.repositories.users.GroupRepository;
import pl.psnc.vtl.portal.repositories.users.UserGroupRepository;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;

import javax.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class GroupPermissionService {

    private final GroupRepository groupRepository;

    private final UserGroupRepository userGroupRepository;
    private  final LoggedInUserHolder loggedInUserHolder;

    public void checkAdminAccess(Long groupId)
            throws AccessDeniedException {
        User user = loggedInUserHolder.getLoggedInUser();

        UserGroupId userGroupId = new UserGroupId(user.getId(), groupId);
        if (!userGroupRepository.existsById(userGroupId)) {
            throw new AccessDeniedException("You cannot access group when you are not in this group");
        }

        UserGroup userGroup = userGroupRepository.getOne(userGroupId);
        UserRole userRole = userGroup.getRole();

        if (!userRole.getCode().equals("admin")) {
            throw new AccessDeniedException("You cannot access group when you are not an administrator of this group");
        }
    }
    public void checkOwnerAccess(Long groupId)
            throws AccessDeniedException {
        User user = loggedInUserHolder.getLoggedInUser();

        Group group = groupRepository.findById(groupId).orElseThrow(
                () -> new AccessDeniedException("You cannot access group that does not exist"));

        if (!group.getCreator().getEmail().equals(user.getEmail())) {
            throw new AccessDeniedException("You cannot access group when you are not the owner of this group");
        }
    }

}
