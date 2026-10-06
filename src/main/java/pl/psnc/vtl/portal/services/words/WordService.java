package pl.psnc.vtl.portal.services.words;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import pl.psnc.vtl.portal.conf.logging.LogAction;
import pl.psnc.vtl.portal.controllers.ErrorResponseMessage;
import pl.psnc.vtl.portal.controllers.RequestFieldsValidationException;
import pl.psnc.vtl.portal.dto.word.TeiElementDto;
import pl.psnc.vtl.portal.model.indexes.Index;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.words.Word;
import pl.psnc.vtl.portal.repositories.indexes.IndexRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.repositories.words.WordRepository;
import pl.psnc.vtl.portal.services.ValidationException;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;
import pl.psnc.vtl.portal.services.verses.UndoRedoService;
import pl.psnc.vtl.portal.services.verses.UndoRedoService.Operation;
import pl.psnc.vtl.portal.services.verses.VerseContentService;
import pl.psnc.vtl.portal.services.verses.VerseService;
import pl.psnc.vtl.portal.utils.BoundingBoxUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WordService {

    private final VerseService verseService;

    private final TranscriptionPermissionService transcriptionPermissionService;

    private final UndoRedoService undoRedoService;

    private final VerseRepository verseRepository;

    private final WordRepository wordRepository;

    private final IndexRepository indexRepository;

    private final EntityManager entityManager;

    private final ApplicationContext applicationContext;

    public Word get(long wordId) {
        return wordRepository.findById(wordId)
                .orElseThrow(() -> new EntityNotFoundException("Word with id " + wordId + " not found"));
    }

    public PageLayer.Id createWords(long verseId, List<Word> words) {
        validate(words, false);
        Verse verse = verseService.getVerseToMakeAction(verseId);

        Operation undoRedo = new Operation();
        undoRedoService.addOperation(verse.getPageLayer().getId(), undoRedo);

        updateVerseContent(verseId, wordStream -> Stream.concat(wordStream, words.stream()), undoRedo);
        doCreateWords(verseId, words);
        List<Long> wordIds = words.stream().map(Word::getId).collect(toList());
        undoRedo.undo(() -> doDeleteWords(verseId, wordIds))
                .redo(() -> doCreateWords(verseId, words));

        return verse.getPageLayer().getId();
    }

    private void doCreateWords(long verseId, List<Word> words) {
        Verse verse = verseRepository.getById(verseId);
        for (Word word : words) {
            word.setVerse(verse);

            for (Index index : word.getIndexes()) {
                index.addVerse(verse);
                verse.addIndex(index);
                Index index2 = indexRepository.findById(index.getId()).orElseGet(() -> indexRepository.save(index));
                index2.addWord(word);
            }
            verse.getWords().add(wordRepository.save(word));
        }
        processCoordinatesChanges(verse);
        verseRepository.save(verse);
    }

    public PageLayer.Id updateWords(long verseId, List<Word> words) {
        validate(words, true);
        Verse verse = verseService.getVerseToMakeAction(verseId);

        List<Long> wordIds = words.stream().map(Word::getId).collect(toList());
        List<Word> oldWords = wordRepository.findAllById(wordIds);
        if (oldWords.size() < words.size() || oldWords.stream().anyMatch(w -> w.getVerse().getId() != verseId)) {
            throw new RequestFieldsValidationException(new ErrorResponseMessage("id", "noSuchWord"));
        }
        oldWords.forEach(entityManager::detach);

        Map<Long, Word> wordsToUpdate = wordRepository.findAllById(wordIds)
                .stream().collect(toMap(Word::getId, w -> w));
        for (Word word : words) {
            Word wordToUpdate = wordsToUpdate.get(word.getId());
            if (word.getContent() != null)
                wordToUpdate.setContent(word.getContent());
            BoundingBox bb = word.getBoundingBox();
            if (bb != null) {
                if (bb.getLowerRightX() != null)
                    wordToUpdate.getBoundingBox().setLowerRightX(bb.getLowerRightX());
                if (bb.getLowerRightY() != null)
                    wordToUpdate.getBoundingBox().setLowerRightY(bb.getLowerRightY());
                if (bb.getUpperLeftX() != null)
                    wordToUpdate.getBoundingBox().setUpperLeftX(bb.getUpperLeftX());
                if (bb.getUpperLeftY() != null)
                    wordToUpdate.getBoundingBox().setUpperLeftY(bb.getUpperLeftY());
            }
        }

        Operation undoRedo = new Operation();
        undoRedoService.addOperation(verse.getPageLayer().getId(), undoRedo);

        updateVerseContent(verseId, wordStream -> wordStream.map(w -> wordsToUpdate.getOrDefault(w.getId(), w)),
                undoRedo);
        wordRepository.saveAll(wordsToUpdate.values());
        processCoordinatesChanges(verseId);
        undoRedo.undo(() -> wordRepository.saveAll(oldWords))
                .redo(() -> wordRepository.saveAll(wordsToUpdate.values()))
                .andAlways(() -> processCoordinatesChanges(verseId));

        return verse.getPageLayer().getId();
    }

    public PageLayer.Id deleteWords(long verseId, List<Long> wordIds) {
        Verse verse = verseService.getVerseToMakeAction(verseId);

        List<Word> words = wordRepository.findAllById(wordIds);
        if (words.size() < wordIds.size() || words.stream().anyMatch(w -> w.getVerse().getId() != verseId)) {
            throw new RequestFieldsValidationException(new ErrorResponseMessage("id", "noSuchWord"));
        }

        Operation undoRedo = new Operation();
        undoRedoService.addOperation(verse.getPageLayer().getId(), undoRedo);

        updateVerseContent(verseId, wordStream -> wordStream.filter(w -> !wordIds.contains(w.getId())), undoRedo);
        doDeleteWords(verseId, wordIds);
        undoRedo.undo(() -> doCreateWords(verseId, words))
                .redo(() -> doDeleteWords(verseId, wordIds));

        return verse.getPageLayer().getId();
    }

    private void doDeleteWords(long verseId, List<Long> wordIds) {
        Verse verse = verseRepository.getById(verseId);
        List<Word> words = wordRepository.findAllById(wordIds);
        for (Word word : words) {
            Set<Index> wordIndexes = new HashSet<>(word.getIndexes());
            if (!wordIndexes.isEmpty())
                word.getIndexes().clear();
            for (Index index : wordIndexes) {
                index.removeWord(word);
                if (verse.getWords().stream().noneMatch(w -> index.getWords().contains(w))) {
                    index.removeVerse(verse);
                    verse.removeIndex(index);
                    if (index.getVerses().isEmpty()) {
                        indexRepository.delete(index);
                    } else {
                        indexRepository.save(index);
                    }
                }
            }
            verse.getWords().remove(word);
            wordRepository.delete(word);
            word.setIndexes(wordIndexes); // for undo-redo
        }
        processCoordinatesChanges(verse);
        verseRepository.save(verse);
    }

    public void replaceWords(long verseId, List<Word> words, Operation undoRedo) {
        Verse verse = verseService.getVerseToMakeAction(verseId);
        List<Word> oldWords = new ArrayList<>(verse.getWords());
        List<Long> oldWordIds = oldWords.stream().map(Word::getId).collect(toList());

        updateVerseContent(verseId, wordStream -> words.stream(), undoRedo);

        doDeleteWords(verseId, oldWordIds);
        undoRedo.undo(() -> doCreateWords(verseId, oldWords))
                .redo(() -> doDeleteWords(verseId, oldWordIds));

        doCreateWords(verseId, words);
        List<Long> wordIds = words.stream().map(Word::getId).collect(toList());
        undoRedo.undo(() -> doDeleteWords(verseId, wordIds))
                .redo(() -> doCreateWords(verseId, words));
    }

    public PageLayer.Id removeTeiGapMarker(long wordId) {
        Word word = wordRepository.findById(wordId).orElseThrow(() -> new EntityNotFoundException("Word not found"));
        checkAccess(word);
        doTeiChange(w -> w.setTeiGap(false), List.of(word));
        LogAction.MODIFY_TRANSCRIPTION.info(log, "Removed TEI GAP element from the word with id: {}", wordId);
        return word.getVerse().getPageLayer().getId();
    }

    public PageLayer.Id removeTeiAddMarker(long wordId) {
        Word word = wordRepository.findById(wordId).orElseThrow(() -> new EntityNotFoundException("Word not found"));
        checkAccess(word);
        doTeiChange(w -> w.setTeiAdd(null), List.of(word));
        LogAction.MODIFY_TRANSCRIPTION.info(log, "Removed TEI ADD element from the word with id: {}", wordId);
        return word.getVerse().getPageLayer().getId();
    }

    public PageLayer.Id removeTeiDelMarker(long wordId) {
        Word word = wordRepository.findById(wordId).orElseThrow(() -> new EntityNotFoundException("Word not found"));
        checkAccess(word);
        doTeiChange(w -> w.setTeiDel(false), List.of(word));
        LogAction.MODIFY_TRANSCRIPTION.info(log, "Removed TEI DEL element from the word with id: {}", wordId);
        return word.getVerse().getPageLayer().getId();
    }

    public PageLayer.Id removeTeiSubstMarker(long wordId) {
        Word word = wordRepository.findById(wordId).orElseThrow(() -> new EntityNotFoundException("Word not found"));
        checkAccess(word);
        doTeiChange(w -> w.setTeiSubst(null), List.of(word));
        LogAction.MODIFY_TRANSCRIPTION.info(log, "Removed TEI SUBST element to the word with id: {}", wordId);
        return word.getVerse().getPageLayer().getId();
    }

    public PageLayer.Id addTeiElementToWords(TeiElementDto teiElementDto) {

        List<Word> wordsToBeModified = wordRepository.findAllById(teiElementDto.getWordsIdentifiers());

        if (wordsToBeModified.size() < teiElementDto.getWordsIdentifiers().size()) {
            throw new javax.validation.ValidationException("List of words is incorrect");
        }

        List<Long> distinctVerseIdentifiers = wordsToBeModified.stream().map(word -> word.getVerse().getId()).distinct().collect(toList());

        if (distinctVerseIdentifiers.size() > 1) {
            throw new javax.validation.ValidationException("All words have to be from one verse only");
        }

        checkAccess(wordsToBeModified.get(0));

        switch (teiElementDto.getTeiElement()) {
            case SUBST:
                doTeiChange(w -> w.setTeiSubst(teiElementDto.getTeiElementValue()), wordsToBeModified);
                break;
            case DEL:
                doTeiChange(w -> w.setTeiDel(true), wordsToBeModified);
                break;
            case ADD:
                doTeiChange(w -> w.setTeiAdd(teiElementDto.getTeiElementValue()), wordsToBeModified);
                break;
            case GAP:
                doTeiChange(w -> w.setTeiGap(true), wordsToBeModified);
                break;
            default:
                throw new javax.validation.ValidationException("Unknown TEI element specified");
        }

        LogAction.MODIFY_TRANSCRIPTION.info(log, "Added TEI {} element to the words with ids: {}",
                teiElementDto.getTeiElement(),
                teiElementDto.getWordsIdentifiers());

        return wordsToBeModified.get(0).getVerse().getPageLayer().getId();
    }

    private void checkAccess(Word word) {
        transcriptionPermissionService.checkTranscribeAccess(word.getVerse().getPage().getTranscription().getId());
    }

    private void doTeiChange(Consumer<Word> change, List<Word> words) {
        /*we have to detach it since this reference and next one (changedWords) would point to the same instances
        (because for Hibernate caching mechanism) */
        words.forEach(entityManager::detach);
        List<Word> changedWords = wordRepository.findAllById(words.stream().map(Word::getId).collect(toList()));
        changedWords.forEach(change);
        undoRedoService.addOperation(words.get(0).getVerse().getPageLayer().getId(), new Operation()
                .undo(() -> wordRepository.saveAll(words))
                .redo(() -> wordRepository.saveAll(changedWords)));
    }

    private void validate(List<Word> words, boolean update) {
        for (Word word : words) {
            var errors = new BeanPropertyBindingResult(word, "Word");
            if (update && word.getId() == null)
                errors.rejectValue("id", "idRequiredForUpdatedWords");
            if (!update && word.getId() != null)
                errors.rejectValue("id", "idNotAllowedForNewWords");

            BoundingBox box = word.getBoundingBox();
            if (box == null) {
                if (!update)
                    errors.rejectValue("boundingBox", "fieldRequired");
            } else {
                errors.pushNestedPath("boundingBox");
                if (box.getLowerRightX() == null)
                    errors.rejectValue("lowerRightX", "fieldRequired");
                if (box.getLowerRightY() == null)
                    errors.rejectValue("lowerRightY", "fieldRequired");
                if (box.getUpperLeftX() == null)
                    errors.rejectValue("upperLeftX", "fieldRequired");
                if (box.getUpperLeftY() == null)
                    errors.rejectValue("upperLeftY", "fieldRequired");
            }
            if (update && word.getContent() == null && box == null)
                errors.rejectValue("content", "contentOrBoundingBoxRequiredForUpdate");
            if (errors.hasErrors())
                throw new ValidationException(errors);
            if (word.getContent() != null)
                word.setContent(word.getContent().replace(" ", "-"));
        }
    }

    private void processCoordinatesChanges(Long verseId) {
        Verse verse = verseRepository.getById(verseId);
        processCoordinatesChanges(verse);
    }

    public static void processCoordinatesChanges(Verse verse) {
        List<Word> verseWords = verse.getWords();
        if (verseWords.isEmpty())
            return;

        Collections.sort(verseWords);
        for (int i = 0; i < verseWords.size(); i++)
            verseWords.get(i).setOrdering(i);

        verse.setBoundingBox(BoundingBoxUtils.getContainingBox(verseWords.stream().map(Word::getBoundingBox)));
    }

    private void updateVerseContent(Long verseId, UnaryOperator<Stream<Word>> wordsChange, Operation undoRedo) {
        List<Word> verseWords = wordRepository.getByVerseIdOrderByOrdering(verseId);
        verseWords = wordsChange.apply(verseWords.stream()).sorted().collect(toList());
        // using as bean from context to work around circular dependency of services
        VerseContentService verseContentService = applicationContext.getBean(VerseContentService.class);
        String newContent = verseWords.stream().map(Word::getContent).collect(joining(" "));
        verseContentService.setContent(verseId, newContent, false, undoRedo);
    }

    public void verseContentChanged(Long verseId, Operation undoRedo) {
        List<Word> oldWords = wordRepository.getByVerseIdOrderByOrdering(verseId);
        if (isEmpty(oldWords))
            return;
        oldWords.forEach(entityManager::detach);

        Verse verse = verseRepository.getById(verseId);
        synchronizeWords(verse);

        List<Word> changedWords = verse.getWords();
        List<Word> deletedWords = oldWords.stream()
                .filter(ow -> changedWords.stream().noneMatch(cw -> ow.getId().equals(cw.getId()))).collect(toList());

        wordRepository.saveAll(changedWords);
        List<Long> deletedWordIds = deletedWords.stream().map(Word::getId).collect(toList());
        doDeleteWords(verseId, deletedWordIds);
        undoRedo.undo(() -> wordRepository.saveAll(oldWords))
                .undo(() -> doCreateWords(verseId, deletedWords))
                .redo(() -> wordRepository.saveAll(changedWords))
                .redo(() -> doDeleteWords(verseId, deletedWordIds));
    }

    public void synchronizeWords(Verse verse) {
        List<Word> verseWords = verse.getWords();
        if (isEmpty(verseWords))
            return;
        if (isBlank(verse.getContent())) {
            verseWords.forEach(w -> w.setContent(""));
            return;
        }
        List<String> stringWords = List.of(verse.getContent().split(" ", verseWords.size()));
        int nonEmptyVerseWords = (int) verseWords.stream().filter(w -> !isBlank(w.getContent())).count();
        while (nonEmptyVerseWords > stringWords.size()) {
            Function<Word, Integer> wordRemoveScore = w -> 0 // remove the lowest score
                    + (stringWords.contains(w.getContent()) ? 100_000 : 0) // don't remove words in the verse
                    + (isBlank(w.getContent()) ? 50_000 : 0) // don't remove already empty words
                    - (w.getContent().replaceAll("\\w+", "").length() * 2000) // remove words with more special chars
                    + (w.getContent().length() * 1000) // remove shorter words
                    - (w.getBoundingBox().getLowerRightX() / 10); // remove words more to the right

            Word toRemove = verseWords.stream().min(comparing(wordRemoveScore)).get();
            verseWords.remove(toRemove);
            nonEmptyVerseWords--;
        }
        Match m = bestMatch(stringWords, verseWords, 0, 0);
        int j = 0;
        while (m != null) {
            for (; j < m.j; j++) {
                verseWords.get(j).setContent("");
            }
            String word = stringWords.get(m.i);
            verseWords.get(j).setContent(word);
            m = m.next;
            j++;
        }
        processCoordinatesChanges(verse);
    }

    private Match bestMatch(List<String> stringWords, List<Word> verseWords, int i, int j) {
        if (i >= stringWords.size() || j >= verseWords.size())
            return null;
        Match m = new Match();
        m.i = i;
        m.j = j;
        m.next = bestMatch(stringWords, verseWords, i + 1, j + 1);
        if (m.next == null && i < stringWords.size() - 1)
            return null;
        m.score = m.next == null ? 0 : m.next.score;
        if (stringWords.get(i).equals(verseWords.get(j).getContent())) {
            m.score += 1000;
        } else if (!isBlank(verseWords.get(j).getContent())) {
            m.score += 10;
        }

        Match m2 = bestMatch(stringWords, verseWords, i, j + 1);
        return (m2 != null && m2.score > m.score) ? m2 : m;
    }

    private static class Match {
        int score;
        int i;
        int j;
        Match next;
    }
}
