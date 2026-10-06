package pl.psnc.vtl.portal.repositories.transcriptions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.psnc.vtl.portal.model.transcriptions.MonographicInfo;

import java.util.List;

public interface MonographicInfoRepository extends JpaRepository<MonographicInfo, Long> {

    @Query("SELECT DISTINCT m FROM MonographicInfo m LEFT JOIN FETCH m.actors WHERE m.id IN :ids")
    List<MonographicInfo> findWithActorsByIdIn(List<Long> ids);
}
