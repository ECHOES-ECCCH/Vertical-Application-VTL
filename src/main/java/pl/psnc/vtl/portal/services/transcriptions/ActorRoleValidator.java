package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import pl.psnc.vtl.portal.dto.transcriptions.ActorRoleId;
import pl.psnc.vtl.portal.model.transcriptions.ActorRole;
import pl.psnc.vtl.portal.repositories.transcriptions.ActorRoleRepository;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ActorRoleValidator {

    private final ActorRoleRepository actorRoleRepository;


    public ActorRole validate(ActorRoleId actorRoleId, Errors errors) {
        if (actorRoleId == null) {
            if (errors != null) {
                errors.rejectValue("code", "transcriptions.error." + TranscriptionErrorMessages.FIELD_REQUIRED.getMessage());
            }
        } else {
            Optional<ActorRole> actorRoleHolder = actorRoleRepository.findById(actorRoleId.getCode());
            if (actorRoleHolder.isEmpty()) {
                if (errors != null) {
                    errors.rejectValue("code", "transcriptions.error." + TranscriptionErrorMessages.FIELD_REQUIRED.getMessage());
                }
            } else {
                return actorRoleHolder.get();
            }
        }
        return null;
    }

}
