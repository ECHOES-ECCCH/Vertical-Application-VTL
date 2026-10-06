package pl.psnc.vtl.portal.repositories.indexes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.indexes.Index;
import pl.psnc.vtl.portal.model.indexes.IndexType;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.words.Word;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndexRepository extends JpaRepository<Index, Long> {

    @Query("SELECT i FROM Index i " +
            "WHERE i.name = :name AND i.type = :indexType AND i.transcriptionId = :transcriptionId")
    Optional<Index> findByNameAndTypeAndTranscription(@Param("name") String name, @Param("indexType") IndexType indexType, @Param("transcriptionId") Long transcriptionId);

    @Query("SELECT DISTINCT i FROM Index i " +
            "WHERE i.transcriptionId = :transcriptionId ORDER BY i.name")
    List<Index> findAllByTranscription(@Param("transcriptionId") Long transcriptionId);

    @Query("SELECT  DISTINCT i FROM Index i " +
            "WHERE (lower(i.name) LIKE lower(:regex) OR lower(i.description) LIKE lower(:regex)) and i.transcriptionId = :transcriptionId")
    List<Index> searchPhraseAndTranscription(@Param("transcriptionId") long transcriptionId, @Param("regex") String regex);

    @Query("SELECT DISTINCT i FROM Index i " +
            "WHERE i.type = :indexType AND (lower(i.name) LIKE lower(:regex) OR lower(i.description) LIKE lower(:regex)) and i.transcriptionId = :transcriptionId")
    List<Index> searchPhraseTypeAndTranscription(@Param("transcriptionId") long transcriptionId, @Param("regex") String regex, @Param("indexType") IndexType indexType);

    @Query("SELECT DISTINCT i FROM Index i JOIN i.verses v WHERE i.type = :indexType AND v.id = :verseId "
            + "ORDER BY i.name")
    List<Index> findAllByVerseIdAndType(@Param("verseId") long verseId, @Param("indexType") IndexType indexType);

	@Query("SELECT v.id, i FROM Index i JOIN i.verses v WHERE v IN :verses")
    List<Object[]> findAllByVerse(List<Verse> verses);

    @Query("SELECT v.id, i FROM Index i JOIN i.words v WHERE v IN :words")
    List<Object[]> findAllByWord(List<Word> words);
}
