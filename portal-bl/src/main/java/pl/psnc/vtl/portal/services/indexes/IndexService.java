package pl.psnc.vtl.portal.services.indexes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import pl.psnc.vtl.portal.conf.logging.LogAction;
import pl.psnc.vtl.portal.dto.indexes.IndexCore;
import pl.psnc.vtl.portal.dto.indexes.IndexDto;
import pl.psnc.vtl.portal.model.indexes.Index;
import pl.psnc.vtl.portal.model.indexes.IndexType;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.words.Word;
import pl.psnc.vtl.portal.repositories.indexes.IndexRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.repositories.words.WordRepository;
import pl.psnc.vtl.portal.services.ValidationException;
import pl.psnc.vtl.portal.services.verses.UndoRedoService;
import pl.psnc.vtl.portal.services.verses.UndoRedoService.Operation;
import pl.psnc.vtl.portal.services.verses.VerseService;
import pl.psnc.vtl.portal.services.words.WordService;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.util.*;

import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IndexService {

    private final IndexRepository indexRepository;

    private final VerseRepository verseRepository;

    private final WordRepository wordRepository;

    private final VerseService verseService;

    private final WordService wordService;

    private final UndoRedoService undoRedoService;

    private final EntityManager entityManager;

    public IndexDto getIndex(Long indexId) {
        return IndexMapper.INSTANCE.toDto(get(indexId));
    }

    private Index get(Long indexId) {
        return indexRepository.findById(indexId)
                .orElseThrow(() -> new IndexException("Unable to find index with id: '" + indexId + "'!"));
    }


    public IndexDto updateIndex(Long indexId, IndexCore indexCore) {
        Index index = update(indexCore, indexId);

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Updated Index with id - {}", index.getId());
        LogAction.clearMDCAction();

        return IndexMapper.INSTANCE.toDto(index);
    }


    public IndexDto createIndex(long transcriptionId, IndexCore indexCore) {

        Long existingId = indexRepository
                .findByNameAndTypeAndTranscription(indexCore.getName(), indexCore.getIndexType(), transcriptionId)
                .map(Index::getId).orElse(null);
        Index index = create(transcriptionId, indexCore, existingId);

        indexRepository.save(index);

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Created Index with id - {}", index.getId());
        LogAction.clearMDCAction();

        return IndexMapper.INSTANCE.toDto(index);
    }


    public void deleteIndex(Long indexId) {
        Index index = get(indexId);

        index.getVerses().forEach(verse -> verse.getIndexes().remove(index));
        index.getVerses().clear();
        index.getWords().forEach(word -> word.getIndexes().remove(index));
        index.getWords().clear();

        indexRepository.delete(index);

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Removed Index with id - {}", index.getId());
        LogAction.clearMDCAction();

    }

    public List<IndexDto> searchIndexes(long transcriptionId, String phrase, String type) {
        List<Index> searchedIndexes;

        if (StringUtils.isBlank(type)) {
            searchedIndexes = indexRepository.searchPhraseAndTranscription(transcriptionId, "%" + phrase + "%");
        } else {
            searchedIndexes = indexRepository.searchPhraseTypeAndTranscription(transcriptionId, "%" + phrase + "%",
                    IndexType.valueOf(type.toUpperCase(Locale.ROOT)));
        }
        searchedIndexes.sort(comparing((Index i) -> score(i, phrase)).thenComparing(Index::getName));

        return IndexMapper.INSTANCE.toDto(searchedIndexes);
    }

    private int score(Index index, String phrase) {
        String nameL = index.getName().toLowerCase(Locale.ROOT);
        String descriptionL = defaultIfBlank(index.getDescription(), "").toLowerCase(Locale.ROOT);
        return index.getName().equals(phrase) ? 0
                : index.getName().startsWith(phrase) ? 1
                : nameL.startsWith(phrase) ? 2
                : nameL.contains(phrase) ? 3
                : descriptionL.equals(phrase) ? 4
                : descriptionL.startsWith(phrase) ? 5
                : descriptionL.contains(phrase) ? 6
                : 7;
    }

    public List<IndexDto> getAllIndexesForTranscription(long transcriptionId) {
        List<Index> indexes = indexRepository.findAllByTranscription(transcriptionId);
        return IndexMapper.INSTANCE.toDto(indexes);
    }

    public IndexDto addIndexToVerse(Long indexId, Long verseId) {
        Operation undoRedo = new Operation();
        Index index = doAddIndexToVerse(indexId, verseId, undoRedo);

        undoRedo.redo(() -> doAddIndexToVerse(indexId, verseId, null))
                .undo(() -> doRemoveIndexFromVerse(indexId, verseId, null));
        undoRedoService.addOperation(verseService.getVerseToMakeAction(verseId).getPageLayer().getId(), undoRedo);

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Added Index with id - {} to Verse with id - {}", indexId, verseId);
        LogAction.clearMDCAction();

        return IndexMapper.INSTANCE.toDto(index);
    }

    public IndexDto addIndexToWord(Long indexId, Long wordId) {
        Operation undoRedo = new Operation();
        Index index = get(indexId);
        Word word = wordService.get(wordId);
        Verse verse = verseService.getVerseToMakeAction(word.getVerse().getId());
        if (!verse.getIndexes().contains(index)) {
            index = doAddIndexToVerse(indexId, verse.getId(), undoRedo);
            undoRedo.redo(() -> doAddIndexToVerse(indexId, verse.getId(), null))
                    .undo(() -> doRemoveIndexFromVerse(indexId, verse.getId(), null));
        }

        doAddIndexToWord(indexId, wordId);
        undoRedo.redo(() -> doAddIndexToWord(indexId, wordId))
                .undo(() -> doRemoveIndexFromWord(indexId, wordId));

        undoRedoService.addOperation(verse.getPageLayer().getId(), undoRedo);

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Added Index with id - {} to Word with id - {}", indexId, wordId);
        LogAction.clearMDCAction();
        return IndexMapper.INSTANCE.toDto(index);
    }

    private Index doAddIndexToVerse(Long indexId, Long verseId, Operation undoRedo) {
        Index index = get(indexId);
        Verse verse = verseService.getVerseToMakeAction(verseId);

        if (index.getVerses().isEmpty() && undoRedo != null) {
            // index just created, undo will delete it, so redo must re-add it
            Index index2 = index;
            entityManager.detach(index2);
            undoRedo.redo(() -> indexRepository.save(index2));
            index = get(indexId);
        }

        if (!index.getVerses().contains(verse)) {
            index.addVerse(verse);
            verse.addIndex(index);
            verseRepository.save(verse);
            indexRepository.save(index);
        }
        return index;
    }

    private Index doAddIndexToWord(Long indexId, Long wordId) {
        Word word = wordService.get(wordId);
        Index index = get(indexId);

        if (!index.getWords().contains(word)) {
            index.addWord(word);
            word.addIndex(index);
            wordRepository.save(word);
            indexRepository.save(index);
        }
        return index;
    }

    public void removeIndexFromVerse(Long indexId, Long verseId) {
        Operation undoRedo = new Operation();
        undoRedo.redo(() -> doRemoveIndexFromVerse(indexId, verseId, null))
                .undo(() -> doAddIndexToVerse(indexId, verseId, null));
        Verse verse = doRemoveIndexFromVerse(indexId, verseId, undoRedo);
        undoRedoService.addOperation(verse.getPageLayer().getId(), undoRedo);

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Removed Index with id - {} from Verse with id - {}", indexId, verseId);
        LogAction.clearMDCAction();
    }

    public void removeIndexFromWord(Long indexId, Long wordId) {
        Operation undoRedo = new Operation();
        Word word = wordService.get(wordId);
        Verse verse = verseService.getVerseToMakeAction(word.getVerse().getId());
        Set<Word> indexWords = get(indexId).getWords();

        undoRedo.redo(() -> doRemoveIndexFromWord(indexId, wordId))
                .undo(() -> doAddIndexToWord(indexId, wordId));
        doRemoveIndexFromWord(indexId, wordId);

        if (verse.getWords().stream().noneMatch(indexWords::contains)) {
            undoRedo.redo(() -> doRemoveIndexFromVerse(indexId, verse.getId(), null))
                    .undo(() -> doAddIndexToVerse(indexId, verse.getId(), null));
            doRemoveIndexFromVerse(indexId, verse.getId(), undoRedo);
        }

        undoRedoService.addOperation(verse.getPageLayer().getId(), undoRedo);

        LogAction.setMDCAction(LogAction.MODIFY_TRANSCRIPTION);
        log.info("Removed Index with id - {} from Word with id - {}", indexId, wordId);
        LogAction.clearMDCAction();

    }

    private Verse doRemoveIndexFromVerse(Long indexId, Long verseId, Operation undoRedo) {
        Index index = get(indexId);
        Verse verse = verseService.getVerseToMakeAction(verseId);
        if (!index.getVerses().contains(verse)) {
            throw new IndexException(
                    "Index with id: '" + indexId + "' is not assigned to verse with id: '" + verseId + "'!");
        }

        verse.getWords().forEach(word -> {
            if (word.getIndexes().contains(index)) {
                doRemoveIndexFromWord(indexId, word.getId());
                if (undoRedo != null)
                    undoRedo.undo(() -> doAddIndexToWord(indexId, word.getId()));
            }
        });

        index.removeVerse(verse);
        verse.removeIndex(index);

        if (index.getVerses().isEmpty()) {
            indexRepository.delete(index);
            if (undoRedo != null)
                undoRedo.undo(() -> indexRepository.save(index));
        } else {
            verseRepository.save(verse);
        }
        return verse;
    }

    private void doRemoveIndexFromWord(Long indexId, Long wordId) {
        Word word = wordService.get(wordId);
        Index index = indexRepository.findById(indexId).orElse(null);
        if (index == null)
            return;

        if (!index.getWords().contains(word)) {
            throw new IndexException(
                    "Index with id: '" + indexId + "' is not assigned to word with id: '" + wordId + "'!");
        }

        index.removeWord(word);
        word.removeIndex(index);
        wordRepository.save(word);
        indexRepository.save(index);
    }

    private Index create(long transcriptionId, IndexCore indexCore, Long indexId) throws ValidationException {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(indexCore, "index");
        Index index = getOrCreateIndex(indexId);

        index.setTranscriptionId(transcriptionId);

        if (StringUtils.isBlank(indexCore.getName())) {
            errors.rejectValue("name", "field.required", "Index name is required.");
        } else {
            index.setName(indexCore.getName().trim());
        }

        if (indexCore.getIndexType() != null) {
            index.setType(indexCore.getIndexType());
        } else {
            errors.rejectValue("type", "field.required", "Index type is required!");
        }

        if (!StringUtils.isBlank(indexCore.getAccessibleOn()) &&
                index.getType().getValue().equals(IndexType.PLACE.getValue())) {
            index.setAccessibleOn(indexCore.getAccessibleOn().trim());
        } else {
            index.setAccessibleOn(null);
        }

        index.setDescription(indexCore.getDescription() == null ? null : indexCore.getDescription().trim());

        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        }

        return index;
    }

    private Index update(IndexCore indexCore, Long indexId) throws ValidationException {
        Index index = getOrCreateIndex(indexId);

        if (!Objects.isNull(indexCore.getName())) {
            index.setName(indexCore.getName().trim());
        }

        if (!StringUtils.isBlank(indexCore.getAccessibleOn()) &&
                index.getType().getValue().equals(IndexType.PLACE.getValue())) {
            index.setAccessibleOn(indexCore.getAccessibleOn().trim());
        } else {
            index.setAccessibleOn(null);
        }

        index.setDescription(indexCore.getDescription() == null ? null : indexCore.getDescription().trim());

        return index;
    }

    private Index getOrCreateIndex(Long indexId) {
        if (indexId != null) {
            return indexRepository.getById(indexId);
        } else {
            return new Index();
        }
    }

}
