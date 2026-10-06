package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionUserCore;
import pl.psnc.vtl.portal.dto.users.GroupIdCore;
import pl.psnc.vtl.portal.model.permissions.TranscriptionGroup;
import pl.psnc.vtl.portal.model.permissions.TranscriptionGroupId;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUser;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUserId;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.users.Group;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.users.UserGroupId;
import pl.psnc.vtl.portal.model.users.UserRole;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionGroupRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionUserRepository;
import pl.psnc.vtl.portal.repositories.users.GroupRepository;
import pl.psnc.vtl.portal.repositories.users.UserGroupRepository;
import pl.psnc.vtl.portal.repositories.users.UserRepository;
import pl.psnc.vtl.portal.repositories.users.UserRoleRepository;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TranscriptionPrivilegesService {

    private final TranscriptionUserRepository transcriptionUserRepository;

    private final TranscriptionRepository transcriptionRepository;

    private final UserRepository userRepository;

    private final UserRoleRepository userRoleRepository;

    private final TranscriptionPermissionService transcriptionPermissionService;

    private final TranscriptionGroupRepository transcriptionGroupRepository;

    private final GroupRepository groupRepository;

    private final UserGroupRepository userGroupRepository;

    private final TranscriptionPrivilegesMailService transcriptionPrivilegesMailService;

    private final Map<TranscriptionUserId, Future<Void>> waitingUpdates = new ConcurrentHashMap<>();

    private final ScheduledExecutorService roleNotificationScheduler = Executors.newSingleThreadScheduledExecutor();

    @Value("${vtl.transcription-privileges-notifications:true}")
    private boolean transcriptionPrivilegesNotifications;

    private Transcription getTranscription(long transcriptionId) {
        return transcriptionRepository.findById(transcriptionId).orElseThrow(
                () -> new EntityNotFoundException("Unable to find Transcription with id '" + transcriptionId + "'!"));
    }

    public List<TranscriptionUser> getUserTranscription(long transcriptionId) {
        transcriptionPermissionService.checkAdminAccess(transcriptionId);
        return transcriptionUserRepository.findByTranscriptionId(transcriptionId);
    }


    private Group getGroup(long groupId) {
        return groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(
                "Unable to find " + Group.class.getName() + " with id '" + groupId + "'!",
                TranscriptionErrorMessages.GROUP_NOT_FOUND.getMessage()));
    }

    private boolean existsUserInGroupForTranscription(long userId, long transcriptionId) {
        List<TranscriptionGroup> transcriptionGroups =
                transcriptionGroupRepository.findByTranscriptionId(transcriptionId);
        for (TranscriptionGroup transcriptionGroup : transcriptionGroups) {
            if (userGroupRepository.existsById(
                    UserGroupId.builder().user(userId).group(transcriptionGroup.getGroup().getId()).build())) {
                return true;
            }
        }
        return false;
    }

    private boolean existsUserDirectlyForTranscription(long userId, long transcriptionId) {
        return transcriptionUserRepository.existsById(
                TranscriptionUserId.builder().transcription(transcriptionId).user(userId).build());
    }


    public boolean existsUserForTranscription(long userId, long transcriptionId) {
        return existsUserDirectlyForTranscription(userId, transcriptionId) || existsUserInGroupForTranscription(userId,
                transcriptionId);
    }


    public List<TranscriptionUser> addUserToTranscription(String email, long transcriptionId, String origin) {
        Transcription transcription = getTranscription(transcriptionId);
        transcriptionPermissionService.checkAdminAccess(transcriptionId);
        
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new NoUserWithGivenEmailException(
                        "User with email " + email + " does not exist",
                        TranscriptionErrorMessages.NO_USER_WITH_GIVEN_EMAIL.getMessage()));

        TranscriptionUserId transcriptionUserId = new TranscriptionUserId(transcriptionId, user.getId());

        if (transcriptionUserRepository.existsById(transcriptionUserId)) {
            throw new TranscriptionUserAlreadyExistsException(
                    "User " + email + " is already in transcription with id " + transcriptionId,
                    TranscriptionErrorMessages.TRANSCRIPTION_USER_ALREADY_EXISTS.getMessage());
        }


        TranscriptionUser transcriptionUser = new TranscriptionUser();
        transcriptionUser.setTranscription(transcription);
        transcriptionUser.setUser(user);
        transcriptionUser.setRole(userRoleRepository.getOne("contributor"));
        transcriptionUserRepository.save(transcriptionUser);

        if (transcriptionPrivilegesNotifications)
            scheduleTask(transcriptionUserId, origin);

        return transcriptionUserRepository.findByTranscriptionId(transcriptionId);
    }


    public List<TranscriptionUser> updateUserRoleInTranscription(TranscriptionUserCore transcriptionUserCore,
                                                                 long transcriptionId, String origin) {
        getTranscription(transcriptionId);
        transcriptionPermissionService.checkAdminAccess(transcriptionId);

        TranscriptionUserId transcriptionUserId = new TranscriptionUserId(transcriptionId,
                transcriptionUserCore.getUserId());

        if (!transcriptionUserRepository.existsById(transcriptionUserId)) {
            throw new EntityNotFoundException(
                    "User " + transcriptionUserCore.getUserId() + " is not in given transcription");
        }

        TranscriptionUser transcriptionUser = transcriptionUserRepository.findById(transcriptionUserId)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find user"));

        UserRole userRole = userRoleRepository.findById(transcriptionUserCore.getRoleCode()).orElseThrow(
                () -> new EntityNotFoundException(
                        "Unable to find " + UserRole.class + " with code '" + transcriptionUserCore.getRoleCode()
                                + "'!"));

        if (transcriptionPrivilegesNotifications && !transcriptionUser.getRole().equals(userRole))
            scheduleTask(transcriptionUserId, origin);

        transcriptionUser.setRole(userRole);
        transcriptionUserRepository.save(transcriptionUser);

        return transcriptionUserRepository.findByTranscriptionId(transcriptionId);
    }


    public List<TranscriptionUser> deleteUserFromTranscription(long userId, long transcriptionId) {
        Transcription transcription = getTranscription(transcriptionId);
        transcriptionPermissionService.checkAdminAccess(transcriptionId);

        if (userId == transcription.getCreator().getId()) {
            throw new TranscriptionCreatorRemovalException("Cannot delete transcription creator from transcription!",
                    "cannotDeleteTranscriptionCreatorFromTranscription");
        }

        TranscriptionUserId transcriptionUserId = new TranscriptionUserId(transcriptionId, userId);
        if (!transcriptionUserRepository.existsById(transcriptionUserId)) {
            throw new EntityNotFoundException(
                    "Unable to find " + TranscriptionUser.class.getName() + " with transcriptionId '" + transcriptionId
                            + "'!");
        }

        transcriptionUserRepository.deleteById(transcriptionUserId);

        return transcriptionUserRepository.findByTranscriptionId(transcriptionId);
    }


    public List<TranscriptionGroup> addGroupToTranscription(GroupIdCore groupIdCore, long transcriptionId) {
        Transcription transcription = getTranscription(transcriptionId);
        transcriptionPermissionService.checkAdminAccess(transcriptionId);

        Group group = getGroup(groupIdCore.getGroupId());

        TranscriptionGroupId transcriptionGroupId = new TranscriptionGroupId(transcriptionId, groupIdCore.getGroupId());
        if (transcriptionGroupRepository.existsById(transcriptionGroupId)) {
            throw new GroupAlreadyExistsInTranscriptionException(
                    "Group with id " + groupIdCore.getGroupId() + " is already in transcription with id "
                            + transcriptionId,
                    TranscriptionErrorMessages.TRANSCRIPTION_GROUP_ALREADY_EXISTS.getMessage());
        }

        TranscriptionGroup transcriptionGroup = TranscriptionGroup.builder().group(group).transcription(transcription)
                .build();
        transcriptionGroupRepository.save(transcriptionGroup);

        return transcriptionGroupRepository.findByTranscriptionId(transcriptionId);
    }


    public List<TranscriptionGroup> deleteGroupFromTranscription(long groupId, long transcriptionId) {
        transcriptionPermissionService.checkAdminAccess(transcriptionId);

        getGroup(groupId);

        TranscriptionGroupId transcriptionGroupId = new TranscriptionGroupId(transcriptionId, groupId);
        if (!transcriptionGroupRepository.existsById(transcriptionGroupId)) {
            throw new EntityNotFoundException(
                    "Unable to find " + TranscriptionGroup.class.getName() + " with transcriptionId '" + transcriptionId
                            + "'!");
        }

        transcriptionGroupRepository.deleteById(transcriptionGroupId);

        return transcriptionGroupRepository.findByTranscriptionId(transcriptionId);
    }

    private Future<Void> scheduleTask(TranscriptionUserId transcriptionUserId, String origin) {
        waitingUpdates.values().removeIf(f -> f.isCancelled() || f.isDone()); //Cleanup
        if (waitingUpdates.containsKey(transcriptionUserId)) {
            waitingUpdates.get(transcriptionUserId).cancel(false);
            waitingUpdates.remove(transcriptionUserId);
        }
        @SuppressWarnings("unchecked")
        Future<Void> future = (Future<Void>) roleNotificationScheduler.schedule(
                () -> transcriptionPrivilegesMailService.sendEmail(transcriptionUserId, origin), 1, TimeUnit.MINUTES);
        waitingUpdates.put(transcriptionUserId, future);

        return future;
    }

}
