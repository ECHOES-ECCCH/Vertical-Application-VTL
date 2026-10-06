package pl.psnc.vtl.portal.repositories.verses;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.verses.Comment;
import pl.psnc.vtl.portal.model.verses.Verse;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    int countByVerse(Verse verse);

    List<Comment> findByVerseIdOrderByDateCreatedDesc(Long verseId);

    List<Comment> findByCreator(User creator);

    void deleteByVerseId(Long verseId);

    void deleteByVersePageId(long pageId);

    void deleteByVersePageTranscriptionId(Long transcriptionId);

    @Query("SELECT c.verse.id, COUNT(c) FROM Comment c WHERE c.verse in :verses GROUP BY c.verse.id")
    List<Long[]> getVerseCommentCounts(List<Verse> verses);

    List<Comment> findByVerseIdIn(List<Long> versesIds);
}
