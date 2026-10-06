package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import pl.psnc.vtl.portal.dto.transcriptions.ActorCore;
import pl.psnc.vtl.portal.model.transcriptions.Actor;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ActorValidator {

    private final ActorRoleValidator actorRoleValidator;


    public List<Actor> validate(List<ActorCore> actorCores, Errors errors, String nestedPath) {
        if (actorCores == null) {
            return List.of();
        }
        List<Actor> actors = new ArrayList<>();
        for (int i = 0; i < actorCores.size(); i++) {
            if (errors != null) {
                errors.pushNestedPath(nestedPath + "[" + i + "]");
            }
            Actor actor = validate(actorCores.get(i), errors);
            if (actor != null) {
                actors.add(actor);
            }
            if (errors != null) {
                errors.popNestedPath();
            }
        }
        return actors;
    }

    private Actor validate(ActorCore actorCore, Errors errors) {
        Actor actor = new Actor();
        if (StringUtils.isNotBlank(actorCore.getName())) {
            actor.setName(actorCore.getName());
        }
        if (errors != null) {
            errors.pushNestedPath("role");
        }
        actor.setRole(actorRoleValidator.validate(actorCore.getRole(), errors));
        if (errors != null) {
            errors.popNestedPath();
        }
        if (actor.getName() != null && actor.getRole() != null) {
            return actor;
        } else {
            return null;
        }
    }

}
