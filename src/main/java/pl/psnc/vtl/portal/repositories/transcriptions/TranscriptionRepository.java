package pl.psnc.vtl.portal.repositories.transcriptions;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionExportDto;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.transcriptions.TranscriptionExportStatus;
import pl.psnc.vtl.portal.model.transcriptions.projections.TranscriptionInfoForDaceProjection;
import pl.psnc.vtl.portal.model.transcriptions.projections.TranscriptionInfoRegularProjection;
import pl.psnc.vtl.portal.model.users.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TranscriptionRepository extends JpaRepository<Transcription, Long>, TranscriptionRepositoryCustom {

    /**
     * @param transcriptionId
     * @return summarize list
     */
//      "         ,count(uvc.id) as corrected   other way to compute this statistic"
    @Query(value = "select  tu.user_role_code as user_role_code ,coalesce(  t.creator_id  = tu.user_id ,false) as isOwner ,  u.email as email, tu.user_id as user_id, " +
            " 		tu.transcription_id as transcription_id , coalesce(stats.accepted,0) as accepted, coalesce(stats.distance,0) as distance " +
            " 		, coalesce(stats.filled,0) as filled, coalesce(stats.corrected,0) as corrected, coalesce(stats.user_rejected,0) as user_rejected " +
            " 		, coalesce(stats2.rejected,0) as rejected " +
            "         FROM transcription_users tu JOIN users u ON tu.user_id = u.id     " +
            "         LEFT JOIN ( SELECT  tu.user_id 				 " +
            " 			,sum( case when ((v.current_verse_content_id =uvc.id or (uvc.id is null and v.content is null) ) and v.status='VERIFIED') then 1 else 0 end) as accepted     " +
            "             ,sum(case when  uvc.distance is not null  then uvc.distance else 0 end ) as distance     " +
            "             ,sum( case when v.current_verse_content_id=uvc.id and ltrim(uvc.plain_content) <> '' then 1 else 0 end ) as filled     " +
            "             ,sum(case WHEN coalesce(  uvc.id<>v.initial_verse_content_id , uvc.id is not null) then 1 else 0 end) as corrected   " +
            "             ,sum( case WHEN     " +
            "                  v.initial_verse_content_id  is not NULL AND    " +
            "                 (  uvc.id <> v.initial_verse_content_id    OR     " +
            "                 ( uvc.id = v.initial_verse_content_id and v.status <> 'VERIFIED') )    " +
            "                 then 1 else 0 end ) as user_rejected     " +
            " 			from " +
            " 			transcription_users tu " +
            "             join pages p   on p.transcription_id = tu.transcription_id " +
            "             JOIN verses v on  v.page_id =p.id      " +
            "             LEFT JOIN verse_contents uvc ON  uvc.verse_id = v.id   and uvc.user_id = tu.user_id     " +
            " 			WHERE tu.transcription_id  = :transcriptionId  	  GROUP BY tu.user_id) stats on stats.user_id =u.id " +
            " 			LEFT JOIN ( " +
            " 				select rejected_by_user_id as user_id, count(*) as rejected  " +
            " 				from transcription_users tu " +
            "                 join pages p   on p.transcription_id = tu.transcription_id " +
            "                 JOIN verses v on  v.page_id =p.id      " +
            "                 JOIN verse_contents uvc ON  uvc.verse_id = v.id   and uvc.user_id = tu.user_id     " +
            " 				WHERE tu.transcription_id  = :transcriptionId   and rejected_by_user_id is not Null      " +
            "               	group by rejected_by_user_id  ) stats2 on stats2.user_id=u.id " +
            "            LEFT JOIN transcriptions t on t.id = tu.transcription_id and t.creator_id = u.id   "+
            " 		   WHERE tu.transcription_id  = :transcriptionId   ", nativeQuery = true)
    List<Object[]> transcriptionSummarize(@Param("transcriptionId") Long transcriptionId);

    @Query("SELECT DISTINCT t FROM Transcription t " +
            "WHERE (t.projectId = :projectId)")
    List<Transcription> findByProjectId(@Param("projectId") String projectId);

    @Query("SELECT distinct t.id AS id, a.title AS analyticInfoTitle, t.datePublished as datePublished, " +
            "t.dateCreated as dateCreated, u.email as creator " +
            "FROM Transcription t, AnalyticInfo a, User u " +
            "WHERE t.externalId=:externalId AND t.hidden=:hidden AND t.analyticInfo=a AND t.creator = u")
    List<TranscriptionInfoForDaceProjection> findAllByExternalIdAndHidden(String externalId, boolean hidden);

    @Query("SELECT t.id AS id, t.quota AS quota, t.usedSpaceQuota AS userSpaceQuota, a.title AS analyticInfoTitle, " +
            "u.id AS ownerId, u.email AS ownerEmail " +
            "FROM Transcription t, User u, AnalyticInfo a " +
            "WHERE t.id IN :ids AND t.creator=u AND t.analyticInfo=a")
    List<TranscriptionInfoRegularProjection> findTranscriptionInfoRegularByIds(List<Long> ids);

    @Query("SELECT new pl.psnc.vtl.portal.dto.transcriptions.TranscriptionExportDto(t.id, t.lastModificationDate as lastModificationDate, a.title, c.email, t.exportStatus) " +
            "FROM Transcription t, User c, AnalyticInfo a " +
            "WHERE t.creator=c and t.analyticInfo=a AND t.exportStatus IN :statuses")
    Page<TranscriptionExportDto> findTranscriptionExportByStatuses(List<TranscriptionExportStatus> statuses, Pageable pageable);

    @Query("SELECT MIN(lastModificationDate) FROM Transcription where hidden=:hidden")
    Optional<LocalDateTime> getMinLastUpdatedDate(boolean hidden);

    Page<Transcription> findAllByHiddenAndLastModificationDateGreaterThanEqualOrderByLastModificationDateAsc(boolean hidden, LocalDateTime form, Pageable pageable);

    Page<Transcription> findAllByHiddenAndLastModificationDateLessThanEqualOrderByLastModificationDateAsc(boolean hidden, LocalDateTime until, Pageable pageable);

    Page<Transcription> findAllByHiddenAndLastModificationDateGreaterThanEqualAndLastModificationDateLessThanEqualOrderByLastModificationDateAsc(boolean hidden, LocalDateTime from, LocalDateTime until, Pageable pageable);

    Page<Transcription> findAllByHiddenOrderByLastModificationDateAsc(boolean hidden, Pageable pageable);

    List<Transcription> findAllByCreator(User user);

}
