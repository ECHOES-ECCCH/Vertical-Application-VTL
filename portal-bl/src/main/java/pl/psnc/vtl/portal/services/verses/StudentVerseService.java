package pl.psnc.vtl.portal.services.verses;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.verses.LearningStatus;
import pl.psnc.vtl.portal.model.verses.StudentVerse;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StudentVerseService {

    private final StudentVerseContentService studentVerseContentService;

    private  final LoggedInUserHolder loggedInUserHolder;

    private final StudentVerseContentCache studentVerseContentCache;

    public List<StudentVerse> completeVerses(List<Verse> verses) {
        User user = java.util.Optional.ofNullable(loggedInUserHolder.getLoggedInUser())
                .orElseThrow(() -> new AccessDeniedException("Public user cannot use student view mode"));

        List<StudentVerse> studentVerses = new ArrayList<>();
        verses.forEach(verse -> {
            StudentVerse studentVerse = new StudentVerse();
            studentVerse.setId(verse.getId());
            studentVerse.setLayerId(verse.getPageLayer().getId().getLayerId());
            studentVerse.setVerseNo(verse.getVerseNo());
            studentVerse.setBoundingBox(verse.getBoundingBox());
            studentVerse.setType(verse.getType());
            studentVerse.setImage(verse.isImage());

            Long transcriptionId = verse.getPage().getTranscription().getId();

            studentVerseContentCache.getStudentVerseContentByUserAndTranscriptionAndId(user.getId(), transcriptionId, verse.getId())
                    .ifPresentOrElse(studentVerseContent -> {
                        studentVerse.setContent(studentVerseContent.getContent());
                        studentVerse.setLearningStatus(studentVerseContentService.
                                computeLearningStatus(studentVerseContent.getContent(), verse.getContent()));
                    }, () -> {
                        studentVerse.setContent(null);
                        studentVerse.setLearningStatus(LearningStatus.NONE);
                    });
            studentVerses.add(studentVerse);
        });
        return studentVerses;
    }
}
