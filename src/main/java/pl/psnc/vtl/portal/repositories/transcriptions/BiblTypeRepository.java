package pl.psnc.vtl.portal.repositories.transcriptions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.transcriptions.BiblType;

@Repository
public interface BiblTypeRepository extends JpaRepository<BiblType, String> {

}
