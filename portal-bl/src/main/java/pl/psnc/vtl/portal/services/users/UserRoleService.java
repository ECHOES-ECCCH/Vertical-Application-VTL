package pl.psnc.vtl.portal.services.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUser;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUserId;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.users.UserRole;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionUserRepository;
import pl.psnc.vtl.portal.repositories.users.UserRoleRepository;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;

    private final TranscriptionUserRepository transcriptionUserRepository;


    public List<UserRole> getUserRoles() {
        return userRoleRepository.findAll(Sort.by("ord"));
    }


    public UserRole getUserRole(String code) {
        Optional<UserRole> userRoleHolder = userRoleRepository.findById(code);
        if (!userRoleHolder.isPresent()) {
            throw new EntityNotFoundException(
                    "Unable to find " + UserRole.class.getName() + " with code '" + code + "'!");
        }
        return userRoleHolder.get();
    }


    public UserRole getUserRoleForTranscription(User user, Transcription transcription) {
        TranscriptionUserId transcriptionUserId = new TranscriptionUserId(transcription.getId(), user.getId());
        TranscriptionUser transcriptionUser = transcriptionUserRepository.getOne(transcriptionUserId);
        return transcriptionUser.getRole();

    }

}
