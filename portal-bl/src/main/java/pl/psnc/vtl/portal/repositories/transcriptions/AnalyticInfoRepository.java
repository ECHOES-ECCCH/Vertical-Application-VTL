package pl.psnc.vtl.portal.repositories.transcriptions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.psnc.vtl.portal.model.transcriptions.AnalyticInfo;

import java.util.Collection;
import java.util.List;

public interface AnalyticInfoRepository extends JpaRepository<AnalyticInfo, Long> {

    @Query("SELECT DISTINCT a FROM AnalyticInfo a LEFT JOIN FETCH a.actors WHERE a.id IN :ids")
    List<AnalyticInfo> findWithActorsByIdIn(Collection<Long> ids);
}
