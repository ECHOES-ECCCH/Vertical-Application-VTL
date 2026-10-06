package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.model.transcriptions.ActorRole;
import pl.psnc.vtl.portal.repositories.transcriptions.ActorRoleRepository;

import javax.transaction.Transactional;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ActorRoleService {

    private final ActorRoleRepository actorRoleRepository;


    public List<ActorRole> getActorRoles() {
        return actorRoleRepository.findAll(Sort.by("ord"));
    }

}
