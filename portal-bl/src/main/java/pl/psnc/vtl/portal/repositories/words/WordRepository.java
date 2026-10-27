package pl.psnc.vtl.portal.repositories.words;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.words.Word;

import java.util.List;
import java.util.Optional;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> {

    @EntityGraph(attributePaths = { "indexes" }, type = EntityGraph.EntityGraphType.LOAD)
    List<Word> getByVerseIdOrderByOrdering(long verseId);

    List<Word> findByVersePageId(long pageId);

    List<Word> findByVersePageLayer(PageLayer pageLayer);

    Optional<List<Word>> findByTeiSubstOrderByOrdering(String teiSubstValue);

    @Query("SELECT w FROM Word w WHERE " +
            " w.verse.page.transcription.id=:transcriptionId AND w.verse.page.pageNo = :pageNo " +
            " and w.verse.pageLayer.layer.id = :layerId " +
            " ORDER BY w.verse.verseNo ASC, w.ordering ASC")
    List<Word> findTranscriptionPageLayerVerseWords(Long transcriptionId, int pageNo, Long layerId);

    List<Word> findAllByVerseIn(List<Verse> verses);
}
