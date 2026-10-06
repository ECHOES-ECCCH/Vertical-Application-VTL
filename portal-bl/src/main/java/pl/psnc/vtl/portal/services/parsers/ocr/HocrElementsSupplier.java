package pl.psnc.vtl.portal.services.parsers.ocr;

import org.apache.commons.lang3.ArrayUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.model.verses.Formatting;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.verses.VerseStatus;
import pl.psnc.vtl.portal.model.words.Word;
import pl.psnc.vtl.portal.services.parsers.DocumentElementsSupplier;
import pl.psnc.vtl.portal.services.words.WordService;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class HocrElementsSupplier extends DocumentElementsSupplier {

    private static final String LINE_SELECTOR = "." + Hocr.OCR_LINE_CLASS_NAME
            + ", ." + Hocr.OCR_CAPTION_CLASS_NAME
            + ", ." + Hocr.OCR_HEADER_CLASS_NAME
            + ", ." + Hocr.OCR_TEXTFLOAT_CLASS_NAME;

    @Override
    public Elements supplyLines(Element element) {
        return element.select(LINE_SELECTOR);
    }


    @Override
    public Verse createVerseWithContentAndBboxOnly(Element element) {
        Verse verse = new Verse();
        verse.setStatus(VerseStatus.NONE);
        verse.setBoundingBox(prepareBoundingBoxForElement(element));

        List<Word> words = element.getElementsByClass(Hocr.OCRX_WORD_CLASS_NAME)
                .stream().map(this::createWord).sorted().collect(toList());
        if (words.isEmpty()) {
            verse.setContent(supplyLines(element).stream().map(Element::html).collect(Collectors.joining()));
        } else {
            verse.setWords(words);
            words.forEach(v -> v.setVerse(verse));
            verse.setContent(words.stream().map(Word::getContent).collect(Collectors.joining(" ")));
            WordService.processCoordinatesChanges(verse);
        }

        verse.setFormatting(new Formatting());
        verse.setVerseNo(verseNumberSupplier.getNextVerseNumber());
        return verse;
    }

    @Override
    public BoundingBox prepareBoundingBoxForElement(Element element) {
        BoundingBox boundingBox = new BoundingBox();
        String[] bBoxCoordinatesFromNode = getBBoxCoordinatesFromNode(element);
        if (bBoxCoordinatesFromNode.length == 4) {
            boundingBox.setUpperLeftX(Integer.parseInt(bBoxCoordinatesFromNode[0]));
            boundingBox.setUpperLeftY(Integer.parseInt(bBoxCoordinatesFromNode[1]));
            boundingBox.setLowerRightX(Integer.parseInt(bBoxCoordinatesFromNode[2]));
            boundingBox.setLowerRightY(Integer.parseInt(bBoxCoordinatesFromNode[3]));
        }
        return boundingBox;
    }


    private String[] getBBoxCoordinatesFromNode(Element node) {
        String title = node.attr("title");

        if (title == null || title.isEmpty())
            return ArrayUtils.EMPTY_STRING_ARRAY;

        if (!title.startsWith("bbox "))
            return ArrayUtils.EMPTY_STRING_ARRAY;

        String bbox = null;
        if (title.contains(";")) {
            bbox = title.substring(title.indexOf("bbox ") + 5, title.indexOf(';'));
        } else {
            bbox = title.substring(title.indexOf("bbox ") + 5);
        }
        return bbox.split(" ");
    }

    private Word createWord(Element element) {
        Word word = new Word();
        word.setBoundingBox(prepareBoundingBoxForElement(element));
        word.setContent(element.html());
        return word;
    }
}
