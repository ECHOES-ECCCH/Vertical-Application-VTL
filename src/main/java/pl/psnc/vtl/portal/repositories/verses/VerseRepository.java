package pl.psnc.vtl.portal.repositories.verses;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.events.Event;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.verses.VerseStatus;
import pl.psnc.vtl.portal.model.verses.projections.VersesStatsForLayer;
import pl.psnc.vtl.portal.model.verses.projections.VersesStatsForPage;

import java.util.List;

@Repository
public interface VerseRepository extends JpaRepository<Verse, Long> {

    List<Verse> findByPageOrderByVerseNoAsc(Page page);

    List<Verse> findByPageLayerOrderByVerseNoAsc(PageLayer pageLayer);

    @Query("SELECT v FROM Verse v WHERE v.page.id = :pageId AND v.pageLayer.layer.id = :layerId ORDER BY v.verseNo ASC")
    List<Verse> findByPageIdAndLayerIdOrderByVerseNoAsc(@Param("pageId") long pageId, @Param("layerId") long layerId);

    int countByPageId(long pageId);

    List<Verse> findByPageLayerIdAndVerseNoBetween(PageLayer.Id pageLayerId, int startVerseNo, int endVerseNo);

    void deleteByPageTranscriptionId(Long transcriptionId);

    @Query("SELECT v from Verse v WHERE v.page.id = :pageId AND v.image = FALSE AND (v.content IS NULL OR v.content = '')")
    List<Verse> findByPageIdAndContentIsNullOrEmptyAndImageIsFalse(@Param("pageId") Long pageId);

    List<Verse> findByPageTranscriptionId(Long transcriptionId);

    List<Verse> findByPageTranscriptionIdOrderByPagePageNoAscVerseNoAsc(Long transcriptionId);

    @Query("SELECT v FROM Verse v WHERE v.page.transcription.id=:transcriptionId AND v.page.pageNo >= :pageNo ORDER BY v.page.pageNo ASC, v.verseNo ASC")
    List<Verse> findByPageTranscriptionIdFromPageNoOrderByPagePageNoAscVerseNoAsc(@Param("transcriptionId") Long transcriptionId, @Param("pageNo") int pageNo);

    @Query("SELECT v FROM Verse v WHERE " +
            " v.page.transcription.id=:transcriptionId AND v.page.pageNo = :pageNo " +
            " and v.pageLayer.layer.id = :layerId " +
            " ORDER BY v.verseNo ASC")
    List<Verse> findTranscriptionPageLayerVerse(Long transcriptionId, int pageNo, Long layerId);

    @Query("SELECT v FROM Verse v WHERE " +
            " v.page.transcription.id=:transcriptionId   " +
            " and v.pageLayer.layer.id = :layerId " +
            " ORDER BY v.page.pageNo ASC, v.verseNo ASC")
    List<Verse> findTranscriptionLayerVerse(Long transcriptionId,  Long layerId);

    List<Verse> findByPage(Page page);

    void deleteByPageIdAndPageLayerLayerId(Long pageId, Long layerId);

    List<Verse> findByPageIdAndPageLayerLayerId(Long pageId, Long layerId);

    @Query("SELECT v.id FROM Verse v WHERE v.pageLayer.id = :pageLayerId")
    List<Long> findVerseIdsByPageLayerId(PageLayer.Id pageLayerId);

    int countByPageIdAndStatus(long pageId, VerseStatus verseStatus);

    int countByPageTranscriptionId(long transcriptionId);

    int countByPageTranscriptionIdAndStatus(long transcriptionId, VerseStatus verseStatus);

    @Query("SELECT v.page.id FROM Verse v JOIN Page p ON v.page = p WHERE p.transcription.id=:transcriptionId AND (v.status = 'NONE' OR v.status = 'TRANSCRIBED') GROUP BY v.page.id")
    List<Long> getFilesIdsToVerification(long transcriptionId);

    @Query("SELECT count(v) FROM Verse v JOIN Page p ON v.page = p WHERE p.transcription.id=:transcriptionId AND (v.status = 'NONE' OR v.status = 'INCORRECT')")
    Long countByTranscriptionAndUntranscribedStatuses(long transcriptionId);

    @Query("SELECT count(v) FROM Verse v JOIN Page p ON v.page = p WHERE p.transcription.id=:transcriptionId AND (v.status = 'NONE' OR v.status = 'INCORRECT' OR v.status = 'TRANSCRIBED')")
    Long countByTranscriptionAndUnverifiedStatuses(long transcriptionId);

    int countByPageLayer(PageLayer pageLayer);

    @Query("SELECT v.status AS status, v.pageLayer.id AS pageLayerId, COUNT(v) AS count FROM Verse v GROUP BY v.status, v.pageLayer.id")
    List<VerseCount> countByStatusAndLayer(long pageId);

    interface VerseCount {
        VerseStatus getStatus();

        PageLayer.Id getPageLayerId();

        long getCount();
    }

    boolean existsByPageIdAndPageLayerLayerId(Long id, Long layerId);

    @Query("SELECT v from Verse v JOIN Page p ON v.page = p WHERE p.transcription.id=:transcriptionId AND fts(polish, v.searchVector, :q, true) = true")
    List<Verse> findAllByTranscriptionIdAndQ(long transcriptionId, String q);

    @Query("SELECT v FROM Verse v WHERE " +
            " v.page.transcription.id=:transcriptionId " +
            " AND v.pageLayer.layer.id=:layerId " +
            " AND fts(polish, v.searchVector, :q, true) = true")
    List<Verse> findAllByTranscriptionIdAndLayerIdAndQ(long transcriptionId, long layerId, String q);

    @Query("SELECT v FROM Verse v WHERE " +
            " v.pageLayer IN :pageLayers " +
            " AND fts(polish, v.searchVector, :q, true) = true")
    List<Verse> findAllByPageLayersAndQ(List<PageLayer> pageLayers, String q);

    List<Verse> findAllByPage_Transcription_IdAndPageLayer_Layer(long transcriptionId, Layer layer);

    @Query("SELECT pl.id.layerId as layerId, " +
            "sum(case when v.status = 'NONE' and v.pageLayer.id.layerId = pl.id.layerId then 1 else 0 end) as noneCount, " +
            "sum(case when v.status = 'TRANSCRIBED' and v.pageLayer.id.layerId = pl.id.layerId then 1 else 0 end) as transcribedCount, " +
            "sum(case when v.status = 'INCORRECT' and v.pageLayer.id.layerId = pl.id.layerId then 1 else 0 end) as incorrectCount, " +
            "sum(case when v.status = 'VERIFIED' and v.pageLayer.id.layerId = pl.id.layerId then 1 else 0 end) as verifiedCount " +
            "FROM Page p LEFT JOIN Verse v ON v.page = p INNER JOIN PageLayer pl ON pl.page = p WHERE p.transcription.id=:transcriptionId GROUP BY pl.id.layerId")
    List<VersesStatsForLayer> getLayersStatistics(long transcriptionId);

    @Query("SELECT p.id as pageId, p.pageNo as pageNo, max(pl.lastModificationDate) as lastModificationDate, max(pl.lastModifiedBy) as lastModifiedBy, " +
            "sum(case when v.status = 'NONE' and v.page.id = p.id and v.pageLayer.id.layerId = pl.id.layerId then 1 else 0 end) as noneCount, " +
            "sum(case when v.status = 'TRANSCRIBED' and v.page.id = p.id and v.pageLayer.id.layerId = pl.id.layerId then 1 else 0 end) as transcribedCount, " +
            "sum(case when v.status = 'INCORRECT' and v.page.id = p.id and v.pageLayer.id.layerId = pl.id.layerId then 1 else 0 end) as incorrectCount, " +
            "sum(case when v.status = 'VERIFIED' and v.page.id = p.id and v.pageLayer.id.layerId = pl.id.layerId then 1 else 0 end) as verifiedCount " +
            "FROM Page p LEFT JOIN Verse v ON v.page = p INNER JOIN PageLayer pl ON pl.page = p WHERE p.transcription.id=:transcriptionId AND pl.id.layerId=:layerId GROUP BY p.id")
    org.springframework.data.domain.Page<VersesStatsForPage> getPagesStatisticsForLayer(long transcriptionId, long layerId, Pageable pageable);

    interface VerseForEventStats {
        String getContent();

        User getModificationLastEventAttendee();

        VerseStatus getStatus();
    }

    List<VerseForEventStats> findByModificationLastEventId(long eventId);

    List<Verse> findByAuthor(User verseAuthor);
    List<Verse> findByAuthorOfLastStatusModification(User verseAuthor);
    List<Verse> findByModificationLastEventAttendee(User verseAuthor);

    List<Verse> findByModificationLastEvent(Event event);
}
