package pl.psnc.vtl.portal.services.transcriptions.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.dto.blocks.BlockDto;
import pl.psnc.vtl.portal.model.blocks.BlockType;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.words.Word;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.services.blocks.BlockService;
import pl.psnc.vtl.portal.services.exports.tei.TeiDocumentComposer;
import pl.psnc.vtl.portal.services.exports.tei.TeiMetadataComposer;
import pl.psnc.vtl.portal.services.exports.utils.TextFileSeparators;
import pl.psnc.vtl.portal.services.exports.utils.XmlPrintingUtil;
import pl.psnc.vtl.portal.services.verses.VerseService;

import javax.transaction.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static pl.psnc.vtl.portal.dto.blocks.BlockDto.onlyBlocks;

@Slf4j
@Service
@Transactional
@RequestScope
@RequiredArgsConstructor
public class TeiTranscriptionContentGenerator implements TranscriptionContentGenerator {

    private final BlockService blockService;
    private final VerseService verseService;

    private final PageRepository pageRepository;

    private Deque<Element> textBodyDivs = new ArrayDeque<>();
    private Deque<Integer> textBodyDivLevels = new ArrayDeque<>();

    private HashMap<BlockDto, String> teiPbValues = new HashMap<>();

    @Override
    public boolean supports(TranscriptionPageContentType transcriptionPageContentType) {
        return transcriptionPageContentType == TranscriptionPageContentType.TEI;
    }

    @Override
    public String generate(Transcription transcription, Layer layer) {
        textBodyDivs.clear();
        textBodyDivLevels.clear();
        teiPbValues.clear();

        Document documentTei = DocumentHelper.createDocument();
        Element teiRoot = documentTei.addElement("TEI");
        teiRoot.addElement("teiHeader");
        Element teiSourceDoc = teiRoot.addElement("sourceDoc");

        List<Page> pages = pageRepository.findByTranscriptionIdOrderByPageNo(transcription.getId());

        List<Object> textBodyBlocks = new ArrayList<>();
        int prevPageStartIndex = 0;

        for (Page page : pages) {
            List<BlockDto> pageBlocks = blockService.getBlocksForExport(page, layer);
            verseService.completeVerses(page);
            teiSourceDoc.add(TeiDocumentComposer.composeNextPageSection(page, pageBlocks));
            prevPageStartIndex += mergeTextBodyBlocks(textBodyBlocks.subList(prevPageStartIndex, textBodyBlocks.size()),
                    new ArrayList<>(pageBlocks), page.getTeiPbValue());
        }

        Element teiText = teiRoot.addElement("text");
        Element teiTextBody = teiText.addElement("body");
        addPreservedTeiBody(teiTextBody, transcription);
        Element baseDiv = teiTextBody.addElement("div");
        baseDiv.addAttribute("type", "VTL");
        textBodyDivs.push(baseDiv);
        textBodyDivLevels.push(0);
        addTextBodyDivs(textBodyBlocks, 0);

        TeiMetadataComposer.addMetadataFromTranscription(documentTei, transcription);

        return XmlPrintingUtil.prettyPrintTei(documentTei);
    }

    private int mergeTextBodyBlocks(List<Object> prevPageItems, List<Object> pageItems, String teiPbValue) {
        BlockDto continuation = onlyBlocks(pageItems).stream().filter(b -> b.isContinuation()).findFirst().orElse(null);
        BlockDto continued = continuation == null ? null : onlyBlocks(prevPageItems).stream()
                .filter(b -> b.getBlockId() != null) // skip dummy from blockService.getBlocksForExport
                .filter(b -> b.getType() == continuation.getType())
                .reduce((b1, b2) -> b2).orElse(null);
        if (continuation == null || continued == null) {
            if (teiPbValue != null) {
                BlockDto dummy = new BlockDto(null, List.of());
                teiPbValues.put(dummy, teiPbValue);
                prevPageItems.add(dummy);
            }
            prevPageItems.addAll(pageItems);
            return prevPageItems.size() - pageItems.size();
        }

        int continuationIndex = pageItems.indexOf(continuation);
        int continuedIndex = prevPageItems.indexOf(continued);

        prevPageItems.subList(continuedIndex + 1, prevPageItems.size()).clear();
        mergeTextBodyBlocks(continued.getChildren(), continuation.getChildren(), teiPbValue);
        prevPageItems.addAll(pageItems.subList(continuationIndex + 1, pageItems.size()));

        return continuedIndex;
    }

    private Element addPreservedTeiBody(Element teiBody, Transcription transcription) {
        if (transcription.isToSingleFileExport() && transcription.getTeiBody() != null) {
            try (InputStream xmlInputStream = new ByteArrayInputStream(transcription.getTeiBody().getBytes())) {
                SAXReader reader = new SAXReader();
                Document tmpBodyDoc = reader.read(xmlInputStream);
                teiBody.setContent(tmpBodyDoc.getRootElement().content());
            } catch (IOException | DocumentException e) {
                log.error("Unable to include teiBody for exported transcription (id = {})", transcription.getId());
            }
        }
    
        return teiBody;
    }

    private void addTextBodyDivs(List<? extends Object> items, int baseLevel) {
        for (Object item : items) {
            if (teiPbValues.containsKey(item)) {
                Element pb = textBodyDivs.peek().addElement("pb");
                pb.addAttribute("n", teiPbValues.get(item));
                continue;
            }
            if (item instanceof Verse) {
                addContent(textBodyDivs.peek(), (Verse) item);
                continue;
            }
            BlockDto block = (BlockDto) item;
            switch (block.getType()) {
            case HEADING_1:
                addTextBodyDivWithHead(baseLevel + 1, block);
                break;
            case HEADING_2:
                addTextBodyDivWithHead(baseLevel + 2, block);
                break;
            case HEADING_3:
                addTextBodyDivWithHead(baseLevel + 3, block);
                break;
            case DIV_PROLOGUE:
                addTextBodyDivWithType(baseLevel, block, "prologue");
                break;
            case DIV_PART:
                addTextBodyDivWithType(baseLevel, block, "part");
                break;
            case DIV_CHAPTER:
                addTextBodyDivWithType(baseLevel, block, "chapter");
                break;
            case DIV_SECTION:
                addTextBodyDivWithType(baseLevel, block, "section");
                break;
            default:
                addContent(textBodyDivs.peek(), new BlockDto(null, List.of(block)));
                break;
            }
        }
    }

    private void addTextBodyDivWithHead(int level, BlockDto block) {
        while (textBodyDivLevels.peek() >= level) {
            textBodyDivs.pop();
            textBodyDivLevels.pop();
        }
        Element newDiv = textBodyDivs.peek().addElement("div");
        textBodyDivs.push(newDiv);
        textBodyDivLevels.push(level);

        Element head = newDiv.addElement("head");

        addContent(head, block);
    }

    private void addTextBodyDivWithType(int baseLevel, BlockDto block, String type) {
        int level = block.getChildren().stream()
                .filter(BlockDto.class::isInstance)
                .map(child -> ((BlockDto) child).getType())
                .mapToInt(t -> t == BlockType.HEADING_1 ? 1
                        : t == BlockType.HEADING_2 ? 2
                        : t == BlockType.HEADING_3 ? 3
                        : 100)
                .min().orElse(1);
        level += baseLevel;

        while (textBodyDivLevels.peek() >= level) {
            textBodyDivs.pop();
            textBodyDivLevels.pop();
        }
        Element newDiv = textBodyDivs.peek().addElement("div");
        newDiv.addAttribute("type", type);
        textBodyDivs.push(newDiv);
        textBodyDivLevels.push(level);

        addTextBodyDivs(block.getChildren(), level);
    }

    private void addContent(Element target, BlockDto block) {
        for (Object child : block.getChildren()) {
            if (teiPbValues.containsKey(child)) {
                Element pb = target.addElement("pb");
                pb.addAttribute("n", teiPbValues.get(child));
                continue;
            }
            if (child instanceof Verse) {
                addContent(target, (Verse) child);
                continue;
            }
            BlockDto childBlock = (BlockDto) child;
            switch (childBlock.getType()) {
            case PARAGRAPH:
                addContent(target.addElement("p"), childBlock);
                break;
            case TABLE:
                addTable(target, childBlock);
                break;
            case LIST:
                addList(target, childBlock);
                break;
            case IMAGE:
                addContent(target.addElement("figure"), childBlock);
                break;
            case CAPTION:
                addContent(target.addElement("head"), childBlock);
                break;
            case HEADER:
                addContent(target.addElement("fw").addAttribute("type", "header"), childBlock);
                break;
            case FOOTER:
                addContent(target.addElement("fw").addAttribute("type", "footer"), childBlock);
                break;
            case PAGE_NO:
                addContent(target.addElement("fw").addAttribute("type", "pageNum"), childBlock);
                break;
            case ANNOTATION:
                addContent(target.addElement("note").addAttribute("type", "footnote"), childBlock);
                break;
            case ADD:
                addContent(target.addElement("add"), childBlock);
                break;
            case ADD_ABOVE:
                addContent(target.addElement("add").addAttribute("place", "above"), childBlock);
                break;
            case ADD_BELOW:
                addContent(target.addElement("add").addAttribute("place", "below"), childBlock);
                break;
            case ADD_MARGIN:
                addContent(target.addElement("add").addAttribute("place", "margin"), childBlock);
                break;
            case DEL:
                addContent(target.addElement("del"), childBlock);
                break;
            case SUBST:
                addContent(target.addElement("subst"), childBlock);
                break;
            }
        }
    }

    private void addTable(Element target, BlockDto block) {
        Element table = target.addElement("table");
        for (Object child : block.getChildren()) {
            if (!(child instanceof BlockDto))
                continue;
            BlockDto rowBlock = (BlockDto) child;
            if (rowBlock.getType() == BlockType.HEADING_1) {
                Element head = table.addElement("head");
                addContent(head, rowBlock);
                continue;
            }
            Element rowElement = table.addElement("row");
            for (Object rowChild : rowBlock.getChildren()) {
                if (!(rowChild instanceof BlockDto) || !((BlockDto) rowChild).getType().equals(BlockType.TABLE_CELL))
                    continue;
                BlockDto cellBlock = (BlockDto) rowChild;
                Element cellElement = rowElement.addElement("cell");
                addContent(cellElement, cellBlock);
            }
        }
    }

    private void addList(Element target, BlockDto block) {
        Element list = target.addElement("list");
        for (Object child : block.getChildren()) {
            if (!(child instanceof BlockDto) || !((BlockDto) child).getType().equals(BlockType.LIST_ITEM))
                continue;
            BlockDto itemBlock = (BlockDto) child;
            Element itemElement = list.addElement("item");
            addContent(itemElement, itemBlock);
        }
    }

    private void addContent(Element element, Verse verse) {
        if (verse.isImage()) {
            element.addElement("graphic").addAttribute("url", verse.getContent());
            return;
        }
        List<Node> newNodes = new ArrayList<>();
        if (CollectionUtils.isEmpty(verse.getWords())) {
            if (!StringUtils.isBlank(verse.getContent()))
                newNodes.add(DocumentHelper.createText(verse.getContent()));
        } else {
            wordsToNodes(verse.getWords(), newNodes, "");
        }

        List<Node> oldNodes = element.content();
        if (!oldNodes.isEmpty() && !newNodes.isEmpty()
                && oldNodes.get(oldNodes.size() - 1) instanceof Text && newNodes.get(0) instanceof Text) {
            Text lastOldNode = (Text) oldNodes.get(oldNodes.size() - 1);
            StringBuilder sb = new StringBuilder(lastOldNode.getText());
            TextFileSeparators.prepareForVerseAppend(sb, newNodes.get(0).getText());
            lastOldNode.setText(sb.toString());
        } else if (!oldNodes.isEmpty()) {
            newNodes.add(0, DocumentHelper.createText(TextFileSeparators.VERSE_SEPARATOR));
        }

        for (Node node : newNodes) {
            element.add(node);
        }
    }

    private void wordsToNodes(List<Word> words2, List<Node> result, String tagToIgnore) {
        List<Word> words = new ArrayList<>(words2);
        for (int i = 0; i < words.size(); i++) {
            Word word = words.get(i);

            if (word.getTeiSubst() != null && !tagToIgnore.contains("subst")) {
                Element substElement = DocumentHelper.createElement("subst");
                result.add(substElement);
                String substId = word.getTeiSubst();
                List<Word> substWords = words.stream().filter(w -> substId.equals(w.getTeiSubst())).collect(toList());
                wordsToNodes(substWords, substElement.content(), tagToIgnore + "subst");

                words.removeAll(substWords);
                i--;
            } else if (word.getTeiAdd() != null && !tagToIgnore.contains("add")) {
                Element addElement = DocumentHelper.createElement("add");
                result.add(addElement);
                String addPlace = word.getTeiAdd();
                if (!addPlace.isEmpty())
                    addElement.addAttribute("place", addPlace);
                List<Word> addWords =
                        words.stream().skip(i).takeWhile(w -> addPlace.equals(w.getTeiAdd())).collect(toList());
                wordsToNodes(addWords, addElement.content(), tagToIgnore + "add");

                words.removeAll(addWords);
                i--;
            } else if (word.isTeiDel() && !tagToIgnore.contains("del")) {
                Element delElement = DocumentHelper.createElement("del");
                result.add(delElement);
                List<Word> delWords = words.stream().skip(i).takeWhile(w -> w.isTeiDel()).collect(toList());
                wordsToNodes(delWords, delElement.content(), tagToIgnore + "del");

                words.removeAll(delWords);
                i--;
            } else if (word.isTeiGap()) {
                result.add(DocumentHelper.createElement("gap"));
            } else {
                result.add(DocumentHelper.createText(word.getContent()));
            }
            if (i < words.size() - 1)
                result.add(DocumentHelper.createText(" "));
        }
    }
}
