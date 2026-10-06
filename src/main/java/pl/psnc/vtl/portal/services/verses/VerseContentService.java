package pl.psnc.vtl.portal.services.verses;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.dto.permissions.ViewMode;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.verses.*;
import pl.psnc.vtl.portal.model.words.Word;
import pl.psnc.vtl.portal.repositories.ocrtools.OCRToolsException;
import pl.psnc.vtl.portal.repositories.ocrtools.OCRToolsRepository;
import pl.psnc.vtl.portal.repositories.ocrtools.OcrBoundingBox;
import pl.psnc.vtl.portal.repositories.verses.VerseContentRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.services.parsers.ocr.HOCRParser;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionExportService;
import pl.psnc.vtl.portal.services.verses.UndoRedoService.Operation;
import pl.psnc.vtl.portal.services.words.WordService;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.ValidationException;

import java.util.*;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Service
@Transactional
@RequiredArgsConstructor
public class VerseContentService {

    public static final LevenshteinDistance distance = new LevenshteinDistance();

    private final VerseRepository verseRepository;

    private final OCRToolsRepository ocrToolsRepository;

    private final HOCRParser hocrParser;

    private final VerseService verseService;

    private final VerseContentRepository verseContentRepository;

    private  final LoggedInUserHolder loggedInUserHolder;

    private final TranscriptionPermissionService transcriptionPermissionService;

    private final TranscriptionExportService transcriptionExportService;

    private final WordService wordService;

    private final UndoRedoService undoRedoService;

    private final EntityManager entityManager;

    public PageLayer.Id updateVerseContent(Long verseId, String content) {
        Verse verse = verseService.getVerseToMakeAction(verseId);

        // TODO sprawdź czy warstwa tekstowa pochodzi z rozpoznania - wówczas znieraj zmiany do wyuczenia/tworzenia nowego profilu (?)
        if (Objects.isNull(content)) content = "";

        if (!verse.getWords().isEmpty())
            content = normalizeFormattingForWords(content);
        if (verse.getCurrentVerseContent() == null || !verse.getCurrentVerseContent().getContent().equals(content)) {
            Operation undoRedo = new Operation();
            setContent(verseId, content, false, undoRedo);
            wordService.verseContentChanged(verseId, undoRedo);
            undoRedoService.addOperation(verse.getPageLayer().getId(), undoRedo);
        }

        return verse.getPageLayer().getId();
    }

    /** Makes sure that all html tags are applied to each word separately. */
    private static String normalizeFormattingForWords(String content) {
        StringBuilder sb = new StringBuilder(content.replaceAll("\\s+", " "));
        ArrayDeque<String> stack = new ArrayDeque<>();
        int tagStart = -1;
        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '<' && tagStart == -1) {
                tagStart = i;
            } else if (sb.charAt(i) == '>' && tagStart != -1) {
                if (sb.charAt(tagStart + 1) == '/') {
                    String tag = sb.substring(tagStart + 2, i);
                    if (!stack.isEmpty() && stack.peek().replaceAll(" .*", "").equals(tag))
                        stack.pop();
                } else {
                    String tag = sb.substring(tagStart + 1, i);
                    stack.push(tag);
                }
                tagStart = -1;
            } else if (sb.charAt(i) == ' ' && tagStart == -1) {
                for (String tag : stack) {
                    sb.insert(i + 1, "<" + tag + ">");
                    String tagBase = tag.replaceAll(" .*", "");
                    sb.insert(i, "</" + tagBase + ">");
                    i += tagBase.length() + 3;
                }
                i += stack.stream().mapToInt(t -> t.length() + 2).sum();
            }
        }
        return sb.toString();
    }

    public PageLayer.Id clearVerseContents(List<Long> verseIds) {
        Verse firstVerse = verseService.getVerseToMakeAction(verseIds.get(0));
        Operation undoRedoOp = new Operation();
        for (Long verseId : verseIds) {
            Verse verse = verseRepository.getById(verseId);
            if (verse == null || !verse.getPageLayer().getId().equals(firstVerse.getPageLayer().getId()))
                throw new ValidationException("verse " + verseId + " not in the same page-layer as " + verseIds.get(0));
            setContent(verseId, "", false, undoRedoOp);
            wordService.verseContentChanged(verseId, undoRedoOp);
        }
        undoRedoService.addOperation(firstVerse.getPageLayer().getId(), undoRedoOp);
        return firstVerse.getPageLayer().getId();
    }

    /**
     * Adds payload (array of bytes) to given verse
     * @param verseId verse identifier
     * @param versePayload payload that should be added to verse
     */
    public void updateVersePayload(Long verseId, VersePayload versePayload) {
        Verse verse = verseService.getVerseToMakeAction(verseId);
        verse.setPayload(versePayload);
    }

    /**
     * Retrieves payload (array of bytes) from given verse. Read access for corresponding transcription is required.
     * @param verseId verse identifier
     * @return payload containing array of bytes
     */
    public VersePayload readVersePayload(Long verseId) {
        Verse verse = verseRepository.findById(verseId)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find verse with id: '" + verseId + "'!"));
        transcriptionPermissionService.checkReadAccess(verse.getPage().getTranscription().getId());
        return verse.getPayload();
    }

    public void deleteVersePayload(Long verseId) {
        Verse verse = verseService.getVerseToMakeAction(verseId);
        verse.setPayload(null);
    }

    public void setContent(Long verseId, String content, boolean isOcr, Operation undoRedo) {
        Verse oldVerse = verseRepository.getById(verseId);
        VerseContent oldContent = oldVerse.getCurrentVerseContent();
        if (oldContent != null && oldContent.getContent().equals(content) && oldContent.isFromOCR() == isOcr)
            return;
        entityManager.detach(oldVerse);
        Verse verse = verseRepository.getById(verseId);

        Long transcriptionId = verse.getPage().getTranscription().getId();
        ViewMode viewMode = transcriptionPermissionService.findViewMode(transcriptionId);

        if(viewMode == ViewMode.TRANSCRIBE || viewMode == ViewMode.VERIFY || viewMode == ViewMode.ADMIN) {
            if(verse.getStatus() == VerseStatus.VERIFIED || verse.getStatus() == VerseStatus.INCORRECT)
                verse.setStatus(VerseStatus.TRANSCRIBED);
        }

        User user = loggedInUserHolder.getLoggedInUser();
        VerseContent newVerseContent = verseContentRepository.save(new VerseContent(user, verse, content, isOcr));
        if (user != null && verse.getCurrentVerseContent() != null) {
            verse.getCurrentVerseContent().setRejectedBy(user);
        }
        verse.setCurrentVerseContent(newVerseContent);
        verse.setAuthorOfLastStatusModification(user);
        if (undoRedo != null) {
            undoRedo.undo(() -> verseContentRepository.delete(newVerseContent))
                    .redo(() -> verseContentRepository.save(newVerseContent));

            undoRedo.undo(() -> verseRepository.save(oldVerse))
                    .redo(() -> verseRepository.save(verse));
        }

        transcriptionExportService.resetTranscriptionExportStatus(verse.getPage().getTranscription().getId());
    }

    public Formatting clearVerseFormatting(Long verseId) {
        Verse verse = verseService.getVerseToMakeAction(verseId);
        verseService.clearVerseFormatting(verse);
        verseRepository.save(verse);
        return verse.getFormatting();
    }


    public Formatting setVerseFormattingFormeWork(Long verseId, FormeWork formeWork) {
        Verse verse = verseService.getVerseToMakeAction(verseId);
        verseService.clearVerseFormatting(verse);
        verse.getFormatting().setFormeWork(formeWork);
        verseRepository.save(verse);
        return verse.getFormatting();
    }


    public Formatting setVerseFormattingHeading(Long verseId, Heading heading) {
        Verse verse = verseService.getVerseToMakeAction(verseId);
        verseService.clearVerseFormatting(verse);
        verse.getFormatting().setHeading(heading);
        verseRepository.save(verse);
        return verse.getFormatting();
    }


    public Formatting setVerseFormattingTabularForm(Long verseId, TabularForm tabularForm) {
        Verse verse = verseService.getVerseToMakeAction(verseId);
        verseService.clearVerseFormatting(verse);
        verse.getFormatting().setTabularForm(tabularForm);
        verseRepository.save(verse);
        return verse.getFormatting();
    }


    public Formatting setVerseFormattingEndOfParagraph(Long verseId) {
        Verse verse = verseService.getVerseToMakeAction(verseId);
        verseService.clearVerseFormatting(verse);
        verse.getFormatting().setEndOfParagraph(true);
        verseRepository.save(verse);
        return verse.getFormatting();

    }


    public Formatting setVerseFormattingListForm(Long verseId, ListForm listForm) {
        Verse verse = verseService.getVerseToMakeAction(verseId);
        verseService.clearVerseFormatting(verse);
        verse.getFormatting().setListForm(listForm);
        verseRepository.save(verse);
        return verse.getFormatting();
    }


    public Formatting setVerseFormattingImageCaption(Long verseId) {
        Verse verse = verseService.getVerseToMakeAction(verseId);
        verseService.clearVerseFormatting(verse);
        verse.getFormatting().setImageCaption(true);
        verseRepository.save(verse);
        return verse.getFormatting();
    }

    public PageLayer.Id recogniseVerses(List<Long> verseIds, List<String> langCodes)
            throws OCRToolsException {
        Verse firstVerse = verseService.getVerseToMakeAction(verseIds.get(0));
        Page page = firstVerse.getPage();
        page.getTranscription().setLastSetLanguageCode(langCodes.get(0));
        Operation undoRedoOp = new Operation();

        List<Verse> verses = verseRepository.findAllById(verseIds);
        if (verses.size() != verseIds.size())
            throw new ValidationException("Some of the verses do not exist!");
        if (verses.stream().map(v -> v.getPageLayer().getId()).distinct().count() != 1)
            throw new ValidationException("All verses should be in the same page-layer!");

        List<OcrBoundingBox> boxes = verses.stream()
                .map(v -> new OcrBoundingBox(v.getId(), v.getBoundingBox())).collect(toList());
        String langs = langCodes.stream().collect(joining("+"));
        Map<Long, String> verseOcrs =
                ocrToolsRepository.recognizePageFragments(page.getTranscription().getId(), page.getPath(),
                        page.getRelativeNo(), boxes, langs, true, false);
        for (Verse verse : verses) {
            List<List<Verse>> parsedVerses = hocrParser.parseVerses(verseOcrs.get(verse.getId()));
            List<Word> words = new ArrayList<>();
            if (!parsedVerses.isEmpty() && !parsedVerses.get(0).isEmpty())
                words = parsedVerses.get(0).get(0).getWords();
            wordService.replaceWords(verse.getId(), words, undoRedoOp);
        }
        undoRedoService.addOperation(firstVerse.getPageLayer().getId(), undoRedoOp);
        return firstVerse.getPageLayer().getId();
    }

    public Formatting setVerseFormattingEndOfNotatedMusic(Long verseId) {
        Verse verse = verseService.getVerseToMakeAction(verseId);
        verseService.clearVerseFormatting(verse);
        verse.getFormatting().setNotatedMusic(true);
        verseRepository.save(verse);
        return verse.getFormatting();

    }

    public Formatting setVerseFormattingAnnotation(Long verseId) {
        Verse verse = verseService.getVerseToMakeAction(verseId);
        verseService.clearVerseFormatting(verse);
        verse.getFormatting().setAnnotation(true);
        verseRepository.save(verse);
        return verse.getFormatting();

    }

    public Formatting setVerseFormattingBold(Long verseId) {
        Verse verse = verseService.getVerseToMakeAction(verseId);
        verseService.clearVerseFormatting(verse);
        verse.getFormatting().setBold(true);
        verseRepository.save(verse);
        return verse.getFormatting();

    }

    public Formatting setVerseFormattingItalic(Long verseId) {
        Verse verse = verseService.getVerseToMakeAction(verseId);
        verseService.clearVerseFormatting(verse);
        verse.getFormatting().setItalic(true);
        verseRepository.save(verse);
        return verse.getFormatting();

    }

    public Formatting setVerseFormattingUnderline(Long verseId) {
        Verse verse = verseService.getVerseToMakeAction(verseId);
        verseService.clearVerseFormatting(verse);
        verse.getFormatting().setUnderline(true);
        verseRepository.save(verse);
        return verse.getFormatting();

    }

    public Formatting setVerseFormattingStrikethrough(Long verseId) {
        Verse verse = verseService.getVerseToMakeAction(verseId);
        verseService.clearVerseFormatting(verse);
        verse.getFormatting().setStrikethrough(true);
        verseRepository.save(verse);
        return verse.getFormatting();

    }

    public Formatting setVerseFormattingMarginalia(Long verseId) {
        Verse verse = verseService.getVerseToMakeAction(verseId);
        verseService.clearVerseFormatting(verse);
        verse.getFormatting().setMarginalia(true);
        verseRepository.save(verse);
        return verse.getFormatting();

    }
}
