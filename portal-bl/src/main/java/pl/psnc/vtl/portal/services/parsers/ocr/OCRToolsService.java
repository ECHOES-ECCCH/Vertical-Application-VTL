package pl.psnc.vtl.portal.services.parsers.ocr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.controllers.ocr.PageRange;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.model.transcriptions.Language;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.verses.VerseStatus;
import pl.psnc.vtl.portal.model.words.Word;
import pl.psnc.vtl.portal.repositories.ocrtools.OCRToolsException;
import pl.psnc.vtl.portal.repositories.ocrtools.OCRToolsRepository;
import pl.psnc.vtl.portal.repositories.ocrtools.OcrBoundingBox;
import pl.psnc.vtl.portal.repositories.ocrtraining.OCRTrainingRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.LanguageRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.repositories.words.WordRepository;
import pl.psnc.vtl.portal.services.layers.LayerService;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;
import pl.psnc.vtl.portal.services.verses.UndoRedoService.Operation;
import pl.psnc.vtl.portal.services.verses.VerseContentService;
import pl.psnc.vtl.portal.services.words.WordService;

import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;
import static pl.psnc.vtl.portal.utils.BoundingBoxUtils.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OCRToolsService {

    private final VerseRepository verseRepository;
    private final WordRepository wordRepository;
    private final LanguageRepository languageRepository;

    private final OCRToolsRepository ocrToolsRepository;
    private final OCRTrainingRepository ocrTrainingRepository;

    private final HOCRParser hocrParser;

    private final LoggedInUserHolder loggedInUserHolder;

    private final LayerService layerService;
    private final VerseContentService verseContentService;
    private final WordService wordService;

    public void recognizeEmptyVerses(Page page, long layerId, String lang) throws OCRToolsException {
        Long transcriptionId = page.getTranscription().getId();
        List<Verse> verses = verseRepository.findByPageIdAndPageLayerLayerId(page.getId(), layerId);
        List<Verse> emptyVerses = verses.stream()
                .filter(v -> StringUtils.isBlank(v.getContent()) && !v.isImage()).collect(toList());
        Map<Long, List<Word>> emptyVersesWords = wordRepository.findAllByVerseIn(emptyVerses).stream()
                .collect(groupingBy(w -> w.getVerse().getId()));

        emptyVerses.forEach(v -> {
            v.setStatus(VerseStatus.NONE);
            v.setInitialVerseContent(null);
            v.setCurrentVerseContent(null);
            v.setLang(lang.replaceFirst("([a-z_]+).*", "$1"));
            verseRepository.save(v);
        });
        verseRepository.flush();

        List<Verse> emptyVersesNoWords = emptyVerses.stream()
                .filter(v -> !emptyVersesWords.containsKey(v.getId())).collect(toList());
        if (!emptyVersesNoWords.isEmpty()) {
            List<OcrBoundingBox> boxes = emptyVersesNoWords.stream()
                    .map(v -> new OcrBoundingBox(v.getId(), v.getBoundingBox())).collect(toList());
            Map<Long, String> ocrResponse = ocrToolsRepository.recognizePageFragments(transcriptionId, page.getPath(),
                    page.getRelativeNo(), boxes, lang, true, false);
            Operation dummyOp = new Operation();
            for (Verse v : emptyVersesNoWords) {
                List<List<Verse>> parsedVerses = hocrParser.parseVerses(ocrResponse.get(v.getId()));
                List<Word> words = new ArrayList<>();
                if (!parsedVerses.isEmpty() && !parsedVerses.get(0).isEmpty())
                    words = parsedVerses.get(0).get(0).getWords();
                wordService.replaceWords(v.getId(), words, dummyOp);
            }
        }

        List<Verse> emptyVersesWithWords = emptyVerses.stream()
                .filter(v -> emptyVersesWords.containsKey(v.getId())).collect(toList());
        if (!emptyVersesWithWords.isEmpty()) {
            List<OcrBoundingBox> boxes = emptyVersesWords.values().stream().flatMap(List::stream)
                    .map(w -> new OcrBoundingBox(w.getId(), w.getBoundingBox())).collect(toList());
            Map<Long, String> ocrResponse = ocrToolsRepository.recognizePageFragments(transcriptionId, page.getPath(),
                    page.getRelativeNo(), boxes, lang, false, true);
            emptyVersesWithWords.forEach(v -> {
                List<Word> words = emptyVersesWords.get(v.getId());
                words.forEach(w -> w.setContent(ocrResponse.getOrDefault(w.getId(), "")));
                String content = words.stream().sorted(comparing(Word::getOrdering))
                        .map(Word::getContent).collect(joining(" "));
                verseContentService.setContent(v.getId(), content, true, null);
            });
        }
    }


    public List<Verse> recognizePage(Page page, String lang, boolean includeWords,
            boolean handleColumns) throws OCRToolsException {
        String filename = page.getPath();
        String extension = FilenameUtils.getExtension(filename);
        if(extension != null && (extension.equals("djvu") || extension.equals("djv"))) {
            filename = page.getIiifUrl().replaceFirst("^\\d+/(.*)/info.json$", "$1");
            try {
                filename = URLDecoder.decode(filename, "UTF-8");
            } catch (Exception e) {
                throw new RuntimeException("Undecodable filename: " + filename, e);
            }
        }

        Long transcriptionId = page.getTranscription().getId();
        String hOCR =
                ocrToolsRepository.recognizePage(transcriptionId, filename, page.getRelativeNo(), lang, handleColumns);

        List<Verse> verses = hocrParser.parseVerses(hOCR).get(0);
        boolean removed = cleanupVerses(verses);
        if (handleColumns) {
            orderVersesInColumns(verses);
        } else if (removed) {
            int verseNo = 1;
            for (Verse v : verses)
                v.setVerseNo(verseNo++);
        }

        for (Verse v : verses) {
            v.setFromOCR(true);
            v.setLang(lang.replaceFirst("([a-z_]+).*", "$1"));
            if (!includeWords) {
                v.getWords().clear();
            }
        }
        return verses;
    }

    private boolean cleanupVerses(List<Verse> verses) {
        boolean removed = false;
        for (Iterator<Verse> it = verses.iterator(); it.hasNext();) {
            Verse v = it.next();
            if ((v.getContent().isBlank() && !v.isImage()) || v.getContent().matches("[|_.]")) {
                it.remove();
                removed = true;
                continue;
            }
            List<Word> words = v.getWords();
            if (words.isEmpty())
                continue;

            boolean cleanFirst = words.get(0).getContent().equals("|");
            if (cleanFirst)
                words.remove(0);
            boolean cleanLast = words.get(words.size() - 1).getContent().equals("|");
            if (cleanLast)
                words.remove(words.size() - 1);
            if (cleanFirst || cleanLast) {
                if (words.isEmpty()) {
                    it.remove();
                    removed = true;
                    continue;
                }
                v.setContent(words.stream().map(Word::getContent).collect(joining(" ")));
                WordService.processCoordinatesChanges(v);
            }
        }
        return removed;
    }

    private void orderVersesInColumns(List<Verse> verses) {
        List<VerseGroup> groups = new ArrayList<>();
        VerseGroup root = new VerseGroup();
        for (Verse v : verses) {
            VerseGroup g = new VerseGroup();
            g.verses.add(v);
            groups.add(g);
        }
        for (VerseGroup g : groups) {
            VerseGroup bestParent = g.findBestParent(groups);
            if (bestParent != null) {
                bestParent.children.add(g);
            } else {
                root.children.add(g);
            }
        }

        root.findSimpleChains();
        root.mergeOverlappingChildren();
        root.mergeVersesWithChildren();
        root.fixOrder();
        root.mergeVersesInSameRow();
        verses.removeIf(v -> Objects.equals(v.getVerseNo(), -1));
        root.addVerseNumbers(1);
    }

    private static class VerseGroup {
        List<Verse> verses = new ArrayList<>();
        List<VerseGroup> children = new ArrayList<>();
    
        VerseGroup findBestParent(List<VerseGroup> groups) {
            BoundingBox gb = verses.get(0).getBoundingBox();
            int gLeft = gb.getUpperLeftX();
            int gRight = gb.getLowerRightX();
            int gTop = gb.getUpperLeftY();
            VerseGroup bestParent = null;
            int bestDist = Integer.MAX_VALUE;
            for (VerseGroup p : groups) {
                BoundingBox pb = p.verses.get(0).getBoundingBox();
                int pLeft = pb.getUpperLeftX();
                int pRight = pb.getLowerRightX();
                int pTop = pb.getUpperLeftY();
                int common = getCommonSegment(pLeft, pRight, gLeft, gRight);
                int dist = gTop - pTop;
                if (common > (gRight - gLeft) * 75 / 100 && dist > 0 && dist < bestDist) {
                    bestDist = dist;
                    bestParent = p;
                }
            }
            return bestParent;
        }

        void findSimpleChains() {
            for (VerseGroup g : children) {
                g.findSimpleChains();
            }
            if (!verses.isEmpty()) {
                for (var it = children.iterator(); it.hasNext();) {
                    VerseGroup g = it.next();
                    if (g.verses.size() == 1 && g.children.isEmpty()) {
                        insertVerse(g.verses.get(0));
                        it.remove();
                    }
                }
            }
            if (children.size() == 1) {
                children.get(0).verses.forEach(this::insertVerse);
                children = children.get(0).children;
            }
        }

        void mergeOverlappingChildren() {
            for (VerseGroup g : children) {
                g.mergeOverlappingChildren();
                g.findSimpleChains();
            }
            while (children.size() > 1) {
                List<BoundingBox> boxes = children.stream()
                        .map(g -> getContainingBox(g.verses.stream().map(v -> v.getBoundingBox()))).collect(toList());
                List<VerseGroup> candidates = children.stream()
                        .filter(g -> g.children.isEmpty())
                        .filter(g -> g.verses.size() < 30)
                        .sorted(comparing(g -> getArea(boxes.get(children.indexOf(g)))))
                        .collect(toList());
                List<Integer> bestPair = null;
                long bestCommon = 0;
                for (int i = 0; i < candidates.size(); i++) {
                    int childreni = children.indexOf(candidates.get(i));
                    BoundingBox boxi = boxes.get(childreni);
                    for (int j = 0; j < children.size(); j++) {
                        if (childreni == j)
                            continue;
                        BoundingBox boxj = boxes.get(j);
                        long common = getCommonArea(boxi, boxj);
                        if (common > bestCommon
                                && (common > getArea(boxi) * 30 / 100 || common > getArea(boxj) * 30 / 100)) {
                            bestCommon = common;
                            bestPair = List.of(childreni, j);
                        }
                    }
                }

                if (bestPair == null)
                    break;
                VerseGroup g1 = children.get(bestPair.get(0));
                VerseGroup g2 = children.get(bestPair.get(1));
                children.remove(g1);
                g1.verses.forEach(g2::insertVerse);
            }
        }

        void mergeVersesWithChildren() {
            for (int i = children.size() - 1; i >= 0; i--) {
                VerseGroup g = children.get(i);
                g.mergeVersesWithChildren();
                if (g.verses.isEmpty()) {
                    children.remove(i);
                    children.addAll(i, g.children);
                }
            }

            for (var it = verses.iterator(); it.hasNext();) {
                Verse v = it.next();
                for (VerseGroup g : children) {
                    if (g.verses.size() < 10)
                        continue;
                    long commonArea = getCommonArea(v.getBoundingBox(),
                            getContainingBox(g.verses.stream().map(Verse::getBoundingBox)));
                    if (commonArea > getArea(v.getBoundingBox()) * 90 / 100) {
                        g.insertVerse(v);
                        it.remove();
                        break;
                    }
                }
            }
        }

        BoundingBox fixOrder() {
            List<BoundingBox> boxes = children.stream().map(g -> g.fixOrder()).collect(toList());
            for (int i = 0; i < boxes.size(); i++) {
                for (int j = i + 1; j < boxes.size(); j++) {
                    BoundingBox b1 = boxes.get(i);
                    BoundingBox b2 = boxes.get(j);
                    boolean swap = b1.getUpperLeftY() > b2.getLowerRightY()
                            || (getCommonSegment(b1.getUpperLeftY(), b1.getLowerRightY(), b2.getUpperLeftY(),
                                    b2.getLowerRightY()) > 0 && b1.getUpperLeftX() > b2.getLowerRightX());
                    if (swap) {
                        Collections.swap(boxes, i, j);
                        Collections.swap(children, i, j);
                    }
                }
            }
            return getContainingBox(Stream.concat(verses.stream().map(v -> v.getBoundingBox()), boxes.stream()));
        }

        void mergeVersesInSameRow() {
            for (VerseGroup g : children) {
                g.mergeVersesInSameRow();
            }
            for (int i = 0; i < verses.size(); i++) {
                BoundingBox bi = verses.get(i).getBoundingBox();
                int j;
                for (j = i + 1; j < verses.size(); j++) {
                    BoundingBox bj = verses.get(j).getBoundingBox();
                    int commonX = getCommonSegment(bi.getUpperLeftX(), bi.getLowerRightX(), bj.getUpperLeftX(),
                            bj.getLowerRightX());
                    int commonY = getCommonSegment(bi.getUpperLeftY(), bi.getLowerRightY(), bj.getUpperLeftY(),
                            bj.getLowerRightY());
                    boolean sameRow = commonX < (bi.getLowerRightX() - bi.getUpperLeftX()) / 8
                            && commonX < (bj.getLowerRightX() - bj.getUpperLeftX()) / 8
                            && (commonY > (bi.getLowerRightY() - bi.getUpperLeftY()) / 2
                                    || commonY > (bj.getLowerRightY() - bj.getUpperLeftY()) / 2);
                    if (!sameRow)
                        break;
                }
                if (j > i + 1) {
                    Verse vi = verses.get(i);
                    List<Verse> subList = verses.subList(i + 1, j);
                    for (Verse v : subList)
                        vi.getWords().addAll(v.getWords());
                    vi.getWords().forEach(w -> w.setVerse(vi));
                    vi.setContent(vi.getWords().stream().map(Word::getContent).collect(joining(" ")));
                    WordService.processCoordinatesChanges(vi);
                    subList.forEach(v -> v.setVerseNo(-1));
                    subList.clear();
                }
            }
        }

        int addVerseNumbers(int verseNo) {
            for (Verse v : verses) {
                v.setVerseNo(verseNo++);
            }
            for (VerseGroup g : children) {
                verseNo = g.addVerseNumbers(verseNo);
            }
            return verseNo;
        }

        void insertVerse(Verse verse) {
            int insertIndex = 0;
            BoundingBox b = verse.getBoundingBox();
            while (insertIndex < verses.size() && is1Above2(verses.get(insertIndex).getBoundingBox(), b)) {
                insertIndex++;
            }
            while (insertIndex < verses.size() && !is1Above2(b, verses.get(insertIndex).getBoundingBox())
                    && b.getUpperLeftX() + (b.getLowerRightX() - b.getUpperLeftX()) / 8
                            > verses.get(insertIndex).getBoundingBox().getLowerRightX()) {
                insertIndex++;
            }
            verses.add(insertIndex, verse);
        }

        boolean is1Above2(BoundingBox b1, BoundingBox b2) {
            if (b1.getLowerRightY() < b2.getUpperLeftY())
                return true;
            if (b1.getLowerRightY() + b1.getUpperLeftY() > b2.getLowerRightY() + b2.getUpperLeftY())
                return false;
            int commonX =
                    getCommonSegment(b1.getUpperLeftX(), b1.getLowerRightX(), b2.getUpperLeftX(), b2.getLowerRightX());
            return commonX > (b1.getLowerRightX() - b1.getUpperLeftX()) / 2
                    || commonX > (b2.getLowerRightX() - b2.getUpperLeftX()) / 2;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString(sb, 0);
            return sb.toString();
        }

        void toString(StringBuilder sb, int level) {
            for (int i = 0; i < level; i++)
                sb.append("  ");
            sb.append("(" + verses.size() + " / " + children.size() + ")").append("\n");
            for (Verse v : verses) {
                for (int i = 0; i < level; i++)
                    sb.append("  ");
                sb.append(v.toString()).append("\n");
            }
            for (VerseGroup g : children) {
                g.toString(sb, level + 1);
            }
        }
    }

    public int trainTesseractFromLayer(Long layerId, List<PageRange> pageRanges, String language) {
        if (!loggedInUserHolder.isCurrentlyLoggedUserSuperadmin())
            throw new AccessDeniedException("Only superadmin can invoke tesseract training");

        List<Verse> versesToTrain = new ArrayList<>();

        Layer layer = layerService.getLayer(layerId);
        for (PageLayer pageLayer : layer.getLayerPages()) {
            if (pageRanges == null || pageRanges.stream().anyMatch(pageRange -> pageRange.isPageInRange(pageLayer.getPage().getPageNo()))) {
                if (pageLayer.getVerses() != null) {
                    versesToTrain.addAll(pageLayer.getVerses().stream()
                            .filter(v -> v.getStatus() == VerseStatus.VERIFIED).collect(toList()));
                }
            }
        }

        ocrTrainingRepository.sendDataToTrainTesseract(versesToTrain, language);
        return versesToTrain.size();
    }

    public List<Language> getOcrLanguages() {
        List<Language> langs = languageRepository.findAll(Sort.by("ord"));
        try {
            ocrToolsRepository.getTrainedLanguages()
                    .forEach(lang -> langs.add(new Language(lang, 0, lang, lang, false)));
        } catch (Exception e) {
            log.error("Failed to get trained languages", e);
        }
        return langs;
    }

    public List<Language> getTrainLanguages() {
        try {
            return ocrTrainingRepository.getTrainableLanguages()
                    .stream()
                    .map(lang -> new Language(lang, 0, lang, lang, false))
                    .collect(toList());
        } catch (Exception e) {
            log.error("Failed to get trainable languages", e);
            return Collections.emptyList();
        }
    }
}
