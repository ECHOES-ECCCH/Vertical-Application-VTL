package pl.psnc.vtl.portal.repositories.pages;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.dto.pages.PageWithHitCount;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.verses.Verse;

import java.util.List;
import java.util.Optional;
import java.util.Set;


@Repository
public interface PageRepository extends JpaRepository<Page, Long> {

    org.springframework.data.domain.Page<Page> findByTranscriptionIdOrderByPageNo(Long transcriptionId,
                                                                                  Pageable pageable);

    Optional<Page> findByTranscriptionIdAndId(Long transcriptionId, Long id);

    Page findByTranscriptionIdAndPageNo(Long transcriptionId, Integer pageNo);

    List<Page> findByTranscriptionIdAndPageNoIn(Long transcriptionId, List<Integer> pageNos);

    Optional<Page> findByTranscriptionIdAndRelativeNoAndPathStartsWithIgnoreCase(Long transcriptionId,
                                                                                 Integer relativeNo, String path);

    List<Page> findByTranscriptionIdOrderByPageNo(Long transcriptionId);

    <T> List<T> findByTranscriptionIdOrderByPageNo(Long transcriptionId, Class<T> type);

    List<Page> findByTranscriptionId(Long transcriptionId);

    @Query("SELECT avg(p.percentOfTranscribed) FROM Page p WHERE p.transcription.id = :transcriptionId "
            + "AND p.empty = false")
    Integer averagePercentOfTranscribedByTranscriptionId(@Param("transcriptionId") Long transcriptionId);

    @Query("SELECT avg(p.percentOfVerified) FROM Page p WHERE p.transcription.id = :transcriptionId "
            + "AND p.empty = false")
    Integer averagePercentOfVerifiedByTranscriptionId(@Param("transcriptionId") Long transcriptionId);

    Set<Page> findByTranscriptionIdAndPath(long transcriptionId, String path);

    boolean existsByTranscriptionIdAndPathStartingWithIgnoreCase(@Param("transcriptionId") long transcriptionId,
                                                                 @Param("path") String path);

    int countByTranscriptionId(long transcriptionId);

    @Query("SELECT p FROM Page p WHERE p.transcription.id = :transcriptionId AND p.pageNo > :pageNo")
    List<Page> findByTranscriptionIdWithNumbersGreaterThan(@Param("transcriptionId") long transcriptionId,
                                                           @Param("pageNo") int pageNo);

    @Query("SELECT  v FROM  Verse v JOIN v.pageLayer pl " +
            " WHERE pl.page.id = :pageId and pl.layer.id = :layerId   ")
    List<Verse> getPageLayerVerses(@Param("pageId") long pageId, @Param("layerId") long layerId);

    @Query("SELECT new pl.psnc.vtl.portal.dto.pages.PageWithHitCount(p, count(p) as hitCount) FROM Page p LEFT JOIN Verse v ON v.page = p " +
            "WHERE p.transcription.id IN :transcriptionsIds AND (fts(polish, v.searchVector, :q, true) = true " +
            "OR v IN (SELECT ve FROM Index i LEFT JOIN i.verses ve " +
            " WHERE fts(polish, i.searchVector, :q, true) = true)) " +
            "GROUP BY p.id " +
            "ORDER BY hitCount DESC")
    List<PageWithHitCount> findByVerseContent(@Param("q") String q,
                                              @Param("transcriptionsIds") List<Long> transcriptionsIds);

    @Query("SELECT p.transcription.id, count(*) FROM Page p " +
            "WHERE p.transcription.id IN :transcriptionsIds " +
            "GROUP BY p.transcription.id")
    List<Long[]> getPagesCounts(List<Long> transcriptionsIds);

    @Query("SELECT DISTINCT p FROM Page p LEFT JOIN FETCH p.pageLayers "
            + "WHERE p.transcription.id IN :transcriptionIds AND p.pageNo BETWEEN :fromPageNo AND :toPageNo")
    List<Page> findWithPageLayersByTranscriptionIdAndPageNo(List<Long> transcriptionIds, Integer fromPageNo,
            Integer toPageNo);
}
