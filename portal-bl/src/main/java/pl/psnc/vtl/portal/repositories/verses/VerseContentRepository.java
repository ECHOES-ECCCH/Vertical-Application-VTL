package pl.psnc.vtl.portal.repositories.verses;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.verses.VerseContent;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerseContentRepository extends JpaRepository<VerseContent, Long> {

    List<VerseContent> findByUser(User verseContentUser);

    List<VerseContent> findByRejectedBy(User verseContentUser);

    @Query("SELECT uvc FROM VerseContent uvc WHERE uvc.user.id=:userId AND uvc.verse.id=:verseId ")
    List<VerseContent> findUserVerseStatus(@Param("userId") long userId, @Param("verseId") long verseId);

    //TODO: test
    @Query("SELECT uvc FROM VerseContent uvc WHERE uvc.user.id=:userId AND uvc.verse.id=:verseId " +
            "order by uvc.updateDate DESC ")
    Optional<VerseContent> getLastUserVerseStatus(@Param("userId") long userId, @Param("verseId") long verseId);


    @Query("SELECT uvc FROM VerseContent uvc WHERE   uvc.verse.id  in :verseIdList " +
            "order by uvc.updateDate DESC ")
    List<VerseContent> findByVerseIdIn(List<Long> verseIdList);

    List<VerseContent> findByVerseId(Long verseId);
}
