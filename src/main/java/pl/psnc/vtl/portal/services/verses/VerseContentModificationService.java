//package pl.psnc.vtl.portal.services.verses;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import pl.psnc.vtl.portal.model.users.User;
//import pl.psnc.vtl.portal.model.verses.VerseContent;
//import pl.psnc.vtl.portal.repositories.verses.VerseContentRepository;
//
//import javax.transaction.Transactional;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class VerseContentModificationService {
//
//    private final VerseContentRepository contentRepository;
//
//    public void saveVerseContentModification(User user, long verseId, String oldContent, String newContent) {
//        VerseContent verseContent = contentRepository.findByUserAndVerseId(user, verseId)
//                .orElseGet(() -> new VerseContent(user, verseId,  newContent, oldContent));
//        verseContent.setContent(newContent);
//        contentRepository.save(verseContent);
//    }
//
//    public void deleteAllVerseContexts(long verseId) {
//        contentRepository.deleteByVerseId(verseId);
//    }
//
//}
