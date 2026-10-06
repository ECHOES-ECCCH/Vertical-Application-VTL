package pl.psnc.vtl.portal.services.verses;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.verses.VerseContent;
import pl.psnc.vtl.portal.model.verses.VerseStatus;
import pl.psnc.vtl.portal.repositories.verses.VerseContentRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TranscriptionVerseStatisticsService {

    private final VerseContentRepository verseContentRepository;

    private final VerseRepository verseRepository;

    private final LevenshteinDistance distance = new LevenshteinDistance();

    private final TranscriptionPermissionService transcriptionPermissionService;

    public List<TranscriptionVerseUserStatistic> getTranscriptionVerseUserStatistics(long transcriptionId) {
        transcriptionPermissionService.checkTranscribeAccess(transcriptionId);
        List<Verse> verses = verseRepository.findByPageTranscriptionId(transcriptionId);
    //TODO: bottleneck , check is levenstein distance could be stored in the row
        Map<User, Integer> usersNumberOfTypedCharacters = usersNumberOfTypedCharacters(verses);
        Map<User, Long> usersVerifiedStatusModification = usersAsAuthorsOfVerifiedStatusModification(verses);
        Map<User, Long> usersStatusModification = usersAsAuthorsOfStatusModification(verses);
        Set<User> usersStatistics = Stream.concat(
                Stream.concat(usersNumberOfTypedCharacters.keySet().stream(),
                            usersStatusModification.keySet().stream()),
                usersVerifiedStatusModification.keySet().stream())
                .collect(Collectors.toSet());

        return usersStatistics.stream()
                .map(k -> new TranscriptionVerseUserStatistic(
                                usersStatusModification.getOrDefault(k, 0L),
                        usersVerifiedStatusModification.getOrDefault(k, 0L),
                        k.getEmail(),
                        usersNumberOfTypedCharacters.getOrDefault(k, 0).longValue()
                ))
                .collect(Collectors.toList());
    }

    Map<User, Long> usersAsAuthorsOfStatusModification(List<Verse> verses) {
        return verses
                .stream()
                .filter(v -> Objects.nonNull(v.getAuthorOfLastStatusModification()))
                .collect(Collectors.groupingBy(Verse::getAuthorOfLastStatusModification, Collectors.counting()));
    }

    Map<User, Long> usersAsAuthorsOfVerifiedStatusModification(List<Verse> verses) {
        return verses
                .stream()
                .filter(v -> v.getStatus().equals(VerseStatus.VERIFIED))
                .filter(v -> Objects.nonNull(v.getAuthorOfLastStatusModification()))
                .collect(Collectors.groupingBy(Verse::getAuthorOfLastStatusModification, Collectors.counting()));
    }

    Map<User, Integer> usersNumberOfTypedCharacters(List<Verse> verses) {
        List<Long> versesIds = verses
                .stream()
                .map(Verse::getId)
                .collect(Collectors.toList());

        Map<User, List<VerseContent>> usersVerseContents = verseContentRepository.findByVerseIdIn(versesIds)
                .stream()
                .collect(Collectors.groupingBy(VerseContent::getUser));

        return usersVerseContents.entrySet()
                .stream()
                .map(this::calculateUserDistanceSum)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    private Map.Entry<User, Integer> calculateUserDistanceSum(Map.Entry<User, List<VerseContent>> userVerseContents) {
        Integer userDistanceSum = userVerseContents.getValue()
                .stream()
                .map(this::calculateDistanceForVerse)
                .reduce(0, Integer::sum);
        return new AbstractMap.SimpleEntry<>(userVerseContents.getKey(), userDistanceSum);

    }


    private Integer calculateDistanceForVerse(VerseContent verseContent) {
        String current =verseContent.getContent()==null?"":verseContent.getContent();
        String old  = "";
        if (verseContent.getOldContent()!= null && verseContent.getOldContent().getContent()!=null){
            old = verseContent.getOldContent().getContent();
        }
        return distance.apply(old,current);
    }


}
