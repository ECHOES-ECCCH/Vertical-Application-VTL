package pl.psnc.vtl.portal.repositories.transcriptions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.transcriptions.ActorRole;

@Repository
public interface ActorRoleRepository extends JpaRepository<ActorRole, String> {

}
