package pl.psnc.vtl.portal.services.exports.mappers;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.util.CollectionUtils;
import pl.psnc.vtl.portal.dto.blocks.BlockDto;
import pl.psnc.vtl.portal.model.blocks.BlockType;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.words.Word;
import pl.psnc.vtl.portal.utils.BoundingBoxUtils;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static pl.psnc.vtl.portal.services.exports.utils.BlockDtoUtil.allVerses;
import static pl.psnc.vtl.portal.services.exports.utils.BlockDtoUtil.blocksOfType;

@RequiredArgsConstructor
public class HOCRMapper {

    public static final double DEFAULT_HOCR_SCALE_FACTOR = 1.0;

    private final Float pdfScaleFactor;

    private static final String BOUNDING_BOX =
            "title=\"bbox ${BBOX_UPPER_LEFT_X} ${BBOX_UPPER_LEFT_Y} ${BBOX_LOWER_RIGHT_X} ${BBOX_LOWER_RIGHT_Y}\"";

    private static final String CAREA =
            "<div class='ocr_carea' id='sec_${PAGE_ID}_${VERSE_ID}' " + BOUNDING_BOX + ">\n";

    private static final Map<BlockType, String> BLOCK_TYPE_TO_HOCR_CLASS;

    static {
        EnumMap<BlockType, String> map = new EnumMap<>(BlockType.class);
        map.put(BlockType.PARAGRAPH,
                CAREA + "<p class='ocr_par' id='sec_${PAGE_ID}_${VERSE_ID}' " + BOUNDING_BOX
                        + ">\n${CONTENT}\n</p>\n</div>\n");
        map.put(BlockType.CAPTION,
            "<div class='ocr_caption' id='block_${PAGE_ID}_${VERSE_ID}' " + BOUNDING_BOX + ">\n"
                    + "<p class='ocr_par' id='par_${PAGE_ID}_${VERSE_ID}' " + BOUNDING_BOX
                        + ">\n${CONTENT}\n</p>\n</div>\n");
        map.put(BlockType.HEADER,
            "<div class='ocr_header' id='block_${PAGE_ID}_${VERSE_ID}' " + BOUNDING_BOX + ">\n"
                    + "<p class='ocr_par' id='par_${PAGE_ID}_${VERSE_ID}' " + BOUNDING_BOX
                        + ">\n${CONTENT}\n</p>\n</div>\n");
        map.put(BlockType.FOOTER,
            "<div class='ocr_footer' id='block_${PAGE_ID}_${VERSE_ID}' " + BOUNDING_BOX + ">\n"
                    + "<p class='ocr_par' id='par_${PAGE_ID}_${VERSE_ID}' " + BOUNDING_BOX
                        + ">\n${CONTENT}\n</p>\n</div>\n");
        map.put(BlockType.ANNOTATION,
                CAREA + "<p class='ocr_par ocr_footnote' id='sec_${PAGE_ID}_${VERSE_ID}' " + BOUNDING_BOX
                        + ">\n${CONTENT}\n</p>\n</div>\n");
        map.put(BlockType.PAGE_NO,
                CAREA + "<div class='ocr_pageno' id='block_${PAGE_ID}_${VERSE_ID}' " + BOUNDING_BOX + ">\n"
                    + "<p class='ocr_par' id='par_${PAGE_ID}_${VERSE_ID}' " + BOUNDING_BOX
                        + ">\n${CONTENT}\n</p>\n</div>\n</div>\n");
        map.put(BlockType.HEADING_1,
                CAREA + "<h1 class='ocr_section' id='sec_${PAGE_ID}_${VERSE_ID}' " + BOUNDING_BOX
                        + ">\n${CONTENT}\n</h1>\n</div>\n");
        map.put(BlockType.HEADING_2,
                CAREA + "<h2 class='ocr_subsection' id='sec_${PAGE_ID}_${VERSE_ID}' " + BOUNDING_BOX
                        + ">\n${CONTENT}\n</h2>\n</div>\n");
        map.put(BlockType.HEADING_3,
                CAREA + "<h3 class='ocr_subsubsection' id='sec_${PAGE_ID}_${VERSE_ID}' " + BOUNDING_BOX
                        + ">\n${CONTENT}\n</h3>\n</div>\n");
        map.put(BlockType.TABLE,
                CAREA + "<table class='ocr_table' id='table_${PAGE_ID}_${VERSE_ID}' " + BOUNDING_BOX
                        + ">\n${CONTENT}</table>\n</div>\n");
        map.put(BlockType.LIST,
                CAREA + "<ul class='ocr_list' id='list_${PAGE_ID}_${VERSE_ID}' " + BOUNDING_BOX
                        + ">\n${CONTENT}</ul>\n</div>\n");

        map.put(BlockType.IMAGE,
                "<div class='ocr_image' id='block_${PAGE_ID}_${VERSE_ID}' " + BOUNDING_BOX
                        + ">\n${CONTENT}\n</div>\n");

        BLOCK_TYPE_TO_HOCR_CLASS = Collections.unmodifiableMap(map);
    }

    private boolean pdfMode = false;

    public void setPdfMode(boolean pdfMode) {
        this.pdfMode = pdfMode;
    }

    public String mapToHocr(Page page, Object blockOrVerse) {
        if (blockOrVerse instanceof Verse) {
            return Stream.of((Verse) blockOrVerse)
                    .flatMap(this::splitVersesForPdf)
                    .map(verse -> getOcrLine(verse, page))
                    .collect(joining());
        }
        BlockDto section = (BlockDto) blockOrVerse;
        String hocrClass = BLOCK_TYPE_TO_HOCR_CLASS.getOrDefault(section.getType(),
                CAREA + "${CONTENT}</div>\n");
        Map<String, String> map = new HashMap<>();
        List<Verse> allVerses = allVerses(section);
        map.put("VERSE_ID", allVerses.get(0).getId().toString());
        map.put("PAGE_ID", page.getId().toString());

        BoundingBox bbox = BoundingBoxUtils.getContainingBox(
                allVerses.stream().map(Verse::getBoundingBox).map(this::rescaledBBox));
        map.put("BBOX_UPPER_LEFT_X", bbox.getUpperLeftX().toString());
        map.put("BBOX_UPPER_LEFT_Y", bbox.getUpperLeftY().toString());
        map.put("BBOX_LOWER_RIGHT_X", bbox.getLowerRightX().toString());
        map.put("BBOX_LOWER_RIGHT_Y", bbox.getLowerRightY().toString());

        map.put("CONTENT", section.getType() == BlockType.TABLE ? getTableContent(page, section)
                : section.getType() == BlockType.LIST ? getListContent(page, section)
                : section.getChildren().stream().map(s -> mapToHocr(page, s)).collect(joining()));

        return new StringSubstitutor(map).replace(hocrClass);
    }

    private String getTableContent(Page page, BlockDto section) {
        StringBuilder result = new StringBuilder();
        for (BlockDto row : blocksOfType(BlockType.TABLE_ROW, section)) {
            result.append("<tr>\n");
            for (BlockDto cell : blocksOfType(BlockType.TABLE_CELL, row)) {
                result.append("<td>\n");
                cell.getChildren().stream().map(s -> mapToHocr(page, s)).forEach(result::append);
                result.append("</td>\n");
            }
            result.append("</tr>\n");

        }
        return result.toString();
    }

    private String getListContent(Page page, BlockDto section) {
        StringBuilder result = new StringBuilder();
        for (BlockDto item : blocksOfType(BlockType.LIST_ITEM, section)) {
            result.append("<li>\n");
            item.getChildren().stream().map(s -> mapToHocr(page, s)).forEach(result::append);
            result.append("</li>\n");
        }
        return result.toString();
    }

    private BoundingBox rescaledBBox(BoundingBox bbox) {
        BoundingBox rescaled = new BoundingBox();
        double scaleFactor = pdfMode ? pdfScaleFactor * 0.01 : DEFAULT_HOCR_SCALE_FACTOR;
        rescaled.setUpperLeftX((int) Math.floor(scaleFactor * bbox.getUpperLeftX()));
        rescaled.setUpperLeftY((int) Math.floor(scaleFactor * bbox.getUpperLeftY()));
        rescaled.setLowerRightX((int) Math.ceil(scaleFactor * bbox.getLowerRightX()));
        rescaled.setLowerRightY((int) Math.ceil(scaleFactor * bbox.getLowerRightY()));
        return rescaled;
    }

    private String getOcrLine(Verse verse, Page page) {
        if (verse.isImage()) {
            return "<img src=\"" + verse.getContent() + "\"/>\n";
        }
        String pageAndVerseNo = page.getPageNo() + "_" + verse.getVerseNo();
        return "<span class='ocr_line' id='line_" + pageAndVerseNo + "' "
                + getBoundingBoxAsHOCRString(verse.getBoundingBox()) + ">" + getOcrLineContent(verse, pageAndVerseNo)
                + "</span>\n";
    }


    private String getOcrLineContent(Verse verse, String pageAndVerseNo) {
        List<Word> words = verse.getWords();
        if (CollectionUtils.isEmpty(words))
            return verse.currentContent();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            Word word = words.get(i);
            sb.append("<span class='ocrx_word' id='word_" + pageAndVerseNo + "_" + (i + 1) + "' "
                    + getBoundingBoxAsHOCRString(word.getBoundingBox()) + ">");
            sb.append(word.getContent());
            sb.append("</span>\n");
        }
        return sb.toString();
    }

    /*
     * PDF viewers sometimes don't recognize spaces between consecutive words that
     * are too close together. A workaround is to only use lines, but split them
     * where spaces between words are large.
     */
    private Stream<Verse> splitVersesForPdf(Verse verse) {
        List<Word> words = verse.getWords();
        if (!pdfMode || CollectionUtils.isEmpty(words))
            return Stream.of(verse);

        List<Verse> result = new ArrayList<>();
        int groupStart = 0;
        for (int i = 1; i <= words.size(); i++) {
            if (i < words.size()) {
                BoundingBox prevBox = words.get(i - 1).getBoundingBox();
                BoundingBox nextBox = words.get(i).getBoundingBox();
                int minHeight = Math.min(prevBox.getLowerRightY() - prevBox.getUpperLeftY(),
                        nextBox.getLowerRightY() - nextBox.getUpperLeftY());
                int dist = nextBox.getUpperLeftX() - prevBox.getLowerRightX();
                if (dist < minHeight)
                    continue; // still the same group
            }
            Verse part = new Verse();
            String content = words.subList(groupStart, i).stream().map(Word::getContent).collect(joining(" "));
            part.setContent(content);
            part.setBoundingBox(BoundingBoxUtils.getContainingBox(words.subList(groupStart, i).stream()
                    .map(Word::getBoundingBox)));
            result.add(part);
            groupStart = i;
        }
        return result.stream();
    }

    private String getBoundingBoxAsHOCRString(BoundingBox bBox) {
        bBox = rescaledBBox(bBox);
        return "title=\"bbox " + bBox.getUpperLeftX() + " " + bBox.getUpperLeftY() + " " + bBox.getLowerRightX() + " "
                + bBox.getLowerRightY() + "\"";
    }


    public static String getHead() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n"
                + "    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" + " <head>\n"
                + "  <title></title>\n" + "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" />\n"
                + "  <meta name='ocr-system' content='Virtual Laboratory Transcription' />\n"
                + "  <meta name='ocr-capabilities' content='ocr_page ocr_carea ocr_par ocr_line ocrx_word ocrp_wconf'/>\n"
                + "</head>\n";
    }


    public String getOcrPage(Page page) {
        BoundingBox bBox = rescaledBBox(page.getBoundingBox());

        return "<div class='ocr_page' id='page_" + page.getPageNo() + "' title='image \"" + page.getPath() + "\"; bbox "
                + bBox.getUpperLeftX() + " " + bBox.getUpperLeftY() + " " + bBox.getLowerRightX() + " " + bBox
                .getLowerRightY() + "; ppageno " + page.getPageNo() + "'>\n";
    }

}
