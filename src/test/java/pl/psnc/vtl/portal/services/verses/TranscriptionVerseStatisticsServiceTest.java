package pl.psnc.vtl.portal.services.verses;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import pl.psnc.vtl.portal.dto.permissions.ViewMode;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.verses.VerseContent;
import pl.psnc.vtl.portal.model.verses.VerseStatus;
import pl.psnc.vtl.portal.repositories.verses.VerseContentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class TranscriptionVerseStatisticsServiceTest {

    @Mock
    VerseContentRepository verseContentRepository;

    @InjectMocks
    TranscriptionVerseStatisticsService statisticsService;


    @Test
    public void shouldReturnUserNumberOfTypedCharacters() {
        //given
        List<Verse> verses = List.of(
                createVerse(1L),
                createVerse(2L),
                createVerse(3L)
        );
        List<VerseContent> records = getRecords();
        List<Long> versesIds = verses.stream().map(Verse::getId).collect(Collectors.toList());
        Mockito.doReturn(records).when(verseContentRepository).findByVerseIdIn(versesIds);
        User user1 = userBy(1L);
        User user2 = userBy(2L);
        //when
        Map<User, Integer> simpleEntries = statisticsService.usersNumberOfTypedCharacters(verses);
        //then
        Assert.assertEquals(2, simpleEntries.size());
        Assert.assertTrue(simpleEntries.containsKey(user1));
        Assert.assertTrue(simpleEntries.containsKey(user2));
        Assert.assertEquals(17L, (long) simpleEntries.get(user1));
        Assert.assertEquals(13L, (long) simpleEntries.get(user2));

    }


    @Test
    public void shouldReturnUsersAsAuthorsOfStatusModification() {

        User user1 = userBy(1L);
        User user2 = userBy(2L);
        List<Verse> userVerses = List.of(
                //user1
                createVerse(user1),
                createVerse(user1),
                createVerifiedVerse(user1),
                //user2
                createVerse(user2),
                createVerse(user2),
                createVerifiedVerse(user2),
                createVerifiedVerse(user2)
        );
        Map<User, Long> map = statisticsService.usersAsAuthorsOfStatusModification(userVerses);
        Assert.assertEquals(2, map.size());
        Assert.assertTrue(map.containsKey(user1));
        Assert.assertEquals(3L, (long) map.get(user1));
        Assert.assertTrue(map.containsKey(user2));
        Assert.assertEquals(4L, (long) map.get(user2));

    }


    @Test
    public void shouldReturnUsersAsAuthorsOfVerifiedStatusModification() {

        User user1 = userBy(1L);
        User user2 = userBy(2L);


        List<Verse> userVerses = List.of(
                //user1
                createVerse(user1),
                createVerse(user1),
                createVerifiedVerse(user1),
                //user2
                createVerse(user2),
                createVerse(user2),
                createVerifiedVerse(user2),
                createVerifiedVerse(user2)
        );

        Map<User, Long> map = statisticsService.usersAsAuthorsOfVerifiedStatusModification(userVerses);
        Assert.assertEquals(2, map.size());
        Assert.assertTrue(map.containsKey(user1));
        Assert.assertEquals(1L, (long) map.get(user1));
        Assert.assertTrue(map.containsKey(user2));
        Assert.assertEquals(2L, (long) map.get(user2));

    }

    private Verse createVerse(long id) {
        Verse v = new Verse();
        v.setId(id);
        return v;
    }

    private Verse createVerse(User user) {
        Verse v = new Verse();
        VerseService.setStatus(List.of(v), ViewMode.VERIFY, VerseStatus.NONE, user);
        return v;
    }

    private Verse createVerifiedVerse(User user) {
        Verse v = new Verse();
        VerseService.setStatus(List.of(v), ViewMode.VERIFY, VerseStatus.VERIFIED, user);
        return v;
    }

    private List<VerseContent> getRecords() {
        Verse v1 = createVerse(1);
        Verse v2 = createVerse(2);
        Verse v3 = createVerse(3);
        List<VerseContent> l = new ArrayList<>();
        VerseContent vc = new VerseContent(1L, userBy(1L), v1, "content");
        v1.setCurrentVerseContent(vc);
        l.add(vc);
        ///
        vc =new VerseContent(2L, userBy(1L), v1, "contentr");
        v1.setCurrentVerseContent(vc);
        l.add(vc);
        ///
        vc =new VerseContent(3L, userBy(2L), v1, "contentaa");
        v1.setCurrentVerseContent(vc);
        l.add(vc);
        ///
        vc =new VerseContent(4L, userBy(1L), v2, "content");
        v2.setCurrentVerseContent(vc);
        l.add(vc);
        ///
        vc =new VerseContent(5L, userBy(1L), v2, "contentrr");
        v2.setCurrentVerseContent(vc);
        l.add(vc);
        ///
        vc =new VerseContent(7L, userBy(2L), v3, "contentqqqq");
        v3.setCurrentVerseContent(vc);
        l.add(vc);
        return l;
    }

    private User userBy(Long id) {
        return User.builder().id(id).build();
    }

}