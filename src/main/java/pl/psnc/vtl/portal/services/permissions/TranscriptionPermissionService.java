package pl.psnc.vtl.portal.services.permissions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.dto.permissions.ViewMode;
import pl.psnc.vtl.portal.model.permissions.TranscriptionGroupId;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUser;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUserId;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.users.SecurityGroup;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.users.UserGroup;
import pl.psnc.vtl.portal.model.users.UserRole;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionGroupRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionUserRepository;
import pl.psnc.vtl.portal.repositories.users.UserGroupRepository;
import pl.psnc.vtl.portal.repositories.users.UserRepository;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TranscriptionPermissionService {
    private  final LoggedInUserHolder loggedInUserHolder;
    private final TranscriptionUserRepository transcriptionUserRepository;

    private final TranscriptionRepository transcriptionRepository;

    private final TranscriptionGroupRepository transcriptionGroupRepository;

    private final UserGroupRepository userGroupRepository;

    private final UserRepository userRepository;


    public ViewMode findViewMode(Long transcriptionId) {
        User user = loggedInUserHolder.getLoggedInUser();
        Transcription transcription = transcriptionRepository.findById(transcriptionId)
                .orElseThrow(EntityNotFoundException::new);

        if (user == null || !user.isEnabled()) {
            if (transcription.isHidden()) {
                return null;
            } else {
                return ViewMode.READ_ONLY;
            }
        } else {
            if(loggedInUserHolder.isCurrentlyLoggedUserSuperadmin()) {
                return ViewMode.ADMIN;
            }

            user = userRepository.findById(user.getId()).orElseThrow(EntityNotFoundException::new);
            TranscriptionUserId transcriptionUserId = new TranscriptionUserId(transcriptionId, user.getId());
            ViewMode modeFromGroup = findUserRoleInGroup(transcriptionId, user.getId());
            if (modeFromGroup == null && (!transcription.isHidden() || user.getSecurityGroup().equals(SecurityGroup.SUPER_ADMIN))) {
                modeFromGroup = ViewMode.READ_ONLY;
            }

            if (!transcriptionUserRepository.existsById(transcriptionUserId)) {
                return modeFromGroup;
            }
            TranscriptionUser transcriptionUser = transcriptionUserRepository.getOne(transcriptionUserId);
            UserRole userRole = transcriptionUser.getRole();

            return getViewModeForGivenCode(modeFromGroup, userRole);
        }
    }


    private ViewMode getViewModeForGivenCode(ViewMode modeFromGroup, UserRole userRole) {
        switch (userRole.getCode()) {
            case "admin":
                return ViewMode.ADMIN;
            case "student":
                return ViewMode.STUDENT;
            case "verifier":
                return getPermissionForVerifier(modeFromGroup);
            case "contributor":
                return getPermissionForContributor(modeFromGroup);
            default:
                throw new UnsupportedOperationException("Unsupported UserRole " + userRole.getCode());
        }
    }


    private ViewMode getPermissionForContributor(ViewMode modeFromGroup) {
        if (!ViewMode.ADMIN.equals(modeFromGroup) && !ViewMode.VERIFY.equals(modeFromGroup)) {
            return ViewMode.TRANSCRIBE;
        } else {
            return modeFromGroup;
        }

    }


    private ViewMode getPermissionForVerifier(ViewMode modeFromGroup) {
        if (!ViewMode.ADMIN.equals(modeFromGroup)) {
            return ViewMode.VERIFY;
        } else {
            return modeFromGroup;
        }
    }


    private ViewMode findUserRoleInGroup(Long transcriptionId, long userId) {
        List<UserGroup> userGroups = userGroupRepository.findByUserId(userId);

        Set<String> userRoles = new HashSet<>();
        for (UserGroup userGroup : userGroups) {
            TranscriptionGroupId transcriptionGroupId = new TranscriptionGroupId(transcriptionId,
                    userGroup.getGroup().getId());
            if (transcriptionGroupRepository.existsById(transcriptionGroupId)) {
                userRoles.add(userGroup.getRole().getCode());
            }

        }

        if (userRoles.contains("admin")) {
            return ViewMode.ADMIN;
        }
        if (userRoles.contains("verifier")) {
            return ViewMode.VERIFY;
        }
        if (userRoles.contains("contributor")) {
            return ViewMode.TRANSCRIBE;
        }
        if (userRoles.contains("student")) {
            return ViewMode.STUDENT;
        }
        return null;
    }


    public ViewMode checkReadAccess(Long transcriptionId)
            throws AccessDeniedException {
        ViewMode viewMode = findViewMode(transcriptionId);
        if (viewMode == null) {
            throw new AccessDeniedException("No access to transcription '" + transcriptionId + "'!");
        }
        return viewMode;
    }


    public ViewMode checkTranscribeAccess(Long transcriptionId)
            throws AccessDeniedException {
        ViewMode viewMode = findViewMode(transcriptionId);
        if (viewMode != ViewMode.TRANSCRIBE && viewMode != ViewMode.VERIFY && viewMode != ViewMode.ADMIN) {
            throw new AccessDeniedException("No transcribe access to transcription '" + transcriptionId + "'!");
        }
        return viewMode;
    }


    public ViewMode checkVerifyAccess(Long transcriptionId)
            throws AccessDeniedException {
        ViewMode viewMode = findViewMode(transcriptionId);
        if (viewMode != ViewMode.VERIFY && viewMode != ViewMode.ADMIN) {
            throw new AccessDeniedException("No verify access to transcription '" + transcriptionId + "'!");
        }
        return viewMode;
    }


    public ViewMode checkAdminAccess(Long transcriptionId)
            throws AccessDeniedException {
        ViewMode viewMode = findViewMode(transcriptionId);
        if (viewMode != ViewMode.ADMIN) {
            throw new AccessDeniedException("No admin access to transcription '" + transcriptionId + "'!");
        }
        return viewMode;
    }

    public ViewMode checkAdminOrVerifyAccess(Long transcriptionId)
            throws AccessDeniedException {
        ViewMode viewMode = findViewMode(transcriptionId);
        if (viewMode != ViewMode.ADMIN && viewMode != ViewMode.VERIFY) {
            throw new AccessDeniedException("No admin or verify access to transcription '" + transcriptionId + "'!");
        }
        return viewMode;
    }

}
