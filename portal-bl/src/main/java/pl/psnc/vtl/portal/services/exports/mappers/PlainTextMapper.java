package pl.psnc.vtl.portal.services.exports.mappers;

import lombok.experimental.UtilityClass;
import pl.psnc.vtl.portal.dto.blocks.BlockDto;
import pl.psnc.vtl.portal.model.blocks.BlockType;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.exports.utils.TextFileSeparators;

import java.util.List;

import static pl.psnc.vtl.portal.services.exports.utils.BlockDtoUtil.allVerses;
import static pl.psnc.vtl.portal.services.exports.utils.BlockDtoUtil.blocksOfType;

@UtilityClass
public class PlainTextMapper {

    public static void toText(BlockDto block, StringBuilder sb) {
        switch (block.getType()) {
        case PARAGRAPH:
        case HEADING_1:
        case HEADING_2:
        case HEADING_3:
        case HEADER:
        case FOOTER:
        case PAGE_NO:
        case DIV_PROLOGUE:
        case DIV_PART:
        case DIV_CHAPTER:
        case DIV_SECTION:
            allVerses(block).forEach(verse -> appendHandleHyphen(sb, verse));
            replaceTrailingWhitespace(sb, TextFileSeparators.SECTION_SEPARATOR);
            break;

        case ANNOTATION:
            allVerses(block).forEach(verse -> appendHandleHyphen(sb, verse));
            replaceTrailingWhitespace(sb, TextFileSeparators.LINE_SEPARATOR);
            break;

        case TABLE:
            blocksOfType(BlockType.TABLE_ROW, block).forEach(row -> {
                toText(row, sb);
                replaceTrailingWhitespace(sb, TextFileSeparators.SECTION_SEPARATOR);
            });
            break;

        case TABLE_ROW:
            blocksOfType(BlockType.TABLE_CELL, block).forEach(cell -> {
                toText(cell, sb);
                replaceTrailingWhitespace(sb, TextFileSeparators.TABLE_CELL_SEPARATOR);
            });
            break;

        case LIST:
            blocksOfType(BlockType.LIST_ITEM, block).forEach(listItem -> {
                sb.append(" - ");
                toText(listItem, sb);
                replaceTrailingWhitespace(sb, TextFileSeparators.LINE_SEPARATOR);
            });
            break;

        case IMAGE:
            blocksOfType(BlockType.CAPTION, block).forEach(row -> {
                toText(row, sb);
                replaceTrailingWhitespace(sb, TextFileSeparators.SECTION_SEPARATOR);
            });
            break;

        default:
            allVerses(block).forEach(verse -> appendHandleHyphen(sb, verse));
        }
    }

    public static String toTextContent(List<Verse> verses) {
        StringBuilder sb = new StringBuilder();
        verses.forEach(verse -> appendHandleHyphen(sb, verse));
        return sb.toString();
    }

    private void replaceTrailingWhitespace(StringBuilder sb, String newWhitespace) {
        while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1)))
            sb.deleteCharAt(sb.length() - 1);
        sb.append(newWhitespace);
    }

    static void appendHandleHyphen(StringBuilder sb, Verse verse) {
        String verseContent = verse.getPlainContent();
        if (verseContent == null) {
            return;
        }
        TextFileSeparators.prepareForVerseAppend(sb, verseContent);
        sb.append(verseContent);
    }
}
