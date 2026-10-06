package pl.psnc.vtl.portal.services.exports.mappers;

import lombok.experimental.UtilityClass;
import pl.psnc.vtl.portal.dto.blocks.BlockDto;
import pl.psnc.vtl.portal.model.blocks.BlockType;
import pl.psnc.vtl.portal.model.indexes.Index;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.words.Word;
import pl.psnc.vtl.portal.services.eBook.HtmlBody.IndexAnchor;
import pl.psnc.vtl.portal.services.exports.utils.TextFileSeparators;

import java.util.HashSet;
import java.util.List;

import static pl.psnc.vtl.portal.services.exports.utils.BlockDtoUtil.allVerses;
import static pl.psnc.vtl.portal.services.exports.utils.BlockDtoUtil.blocksOfType;

@UtilityClass
public class HtmlMapper {

    public void toHtml(Object item, StringBuilder sb, List<IndexAnchor> indexAnchors) {
        if (item instanceof Verse) {
            appendVerse(sb, (Verse) item, indexAnchors);
            return;
        }
        StringBuilder sb2 = new StringBuilder();
        BlockDto block = (BlockDto) item;
        switch (block.getType()) {
        case PARAGRAPH:
        case ANNOTATION:
            block.getChildren().forEach(child -> toHtml(child, sb2, indexAnchors));
            sb.append("<p class='html_par' >\n");
            sb.append(sb2.toString());
            sb.append("\n</p>\n");
            break;

        case HEADING_1:
            block.getChildren().forEach(child -> toHtml(child, sb2, indexAnchors));
            sb.append("<h1 class='html_par' >\n");
            sb.append(sb2.toString());
            sb.append("\n</h1>\n");
            break;

        case HEADING_2:
            block.getChildren().forEach(child -> toHtml(child, sb2, indexAnchors));
            sb.append("<h2 class='html_par' >\n");
            sb.append(sb2.toString());
            sb.append("\n</h2>\n");
            break;

        case HEADING_3:
            block.getChildren().forEach(child -> toHtml(child, sb2, indexAnchors));
            sb.append("<h3 class='html_par' >\n");
            sb.append(sb2.toString());
            sb.append("\n</h3>\n");
            break;

        case TABLE:
            blocksOfType(BlockType.TABLE_ROW, block).forEach(row -> toHtml(row, sb2, indexAnchors));
            sb.append("<table>\n");
            sb.append(sb2.toString());
            sb.append("\n</table>\n");
            break;

        case TABLE_ROW:
            blocksOfType(BlockType.TABLE_CELL, block).forEach(cell -> toHtml(cell, sb2, indexAnchors));
            sb.append("<tr>\n");
            sb.append(sb2.toString());
            sb.append("\n</tr>\n");
            break;

        case TABLE_CELL:
            block.getChildren().forEach(child -> toHtml(child, sb2, indexAnchors));
            sb.append("<td>\n");
            sb.append(sb2.toString());
            sb.append("\n</td>\n");
            break;

        case LIST:
            blocksOfType(BlockType.LIST_ITEM, block).forEach(listItem -> toHtml(listItem, sb2, indexAnchors));
            sb.append("<ul>\n");
            sb.append(sb2.toString());
            sb.append("\n</ul>\n");
            break;

        case LIST_ITEM:
            block.getChildren().forEach(child -> toHtml(child, sb2, indexAnchors));
            sb.append("<li>");
            sb.append(sb2.toString());
            sb.append("</li>\n");
            break;

        case IMAGE:
            block.getChildren().forEach(child -> toHtml(child, sb2, indexAnchors));
            sb.append("<figure>\n");
            sb.append(sb2.toString());
            sb.append("\n</figure>\n");
            break;

        case CAPTION:
            block.getChildren().forEach(child -> toHtml(child, sb2, indexAnchors));
            sb.append("<figcaption>\n");
            sb.append(sb2.toString());
            sb.append("\n</figcaption>\n");
            break;

        default:
            allVerses(block).forEach(verse -> appendVerse(sb, verse, indexAnchors));
        }
    }

    private static void appendVerse(StringBuilder sb, Verse verse, List<IndexAnchor> indexAnchors) {
        String verseContent = verse.getContent();
        if (verseContent == null)
            return;
        if (verse.isImage()) {
            sb.append("<img src=\"" + verseContent + "\" />");
            return;
        }
        TextFileSeparators.prepareForVerseAppend(sb, verseContent);
        if (verse.getIndexes().isEmpty()) {
            sb.append(verseContent);
            return;
        }
        HashSet<Index> indexes = new HashSet<>(verse.getIndexes());
        StringBuilder sb2 = new StringBuilder();
        for (Word word : verse.getWords()) {
            for (Index index : word.getIndexes()) {
                String anchorId = "i" + index.getId() + "w" + word.getId();
                sb2.append("<a id=\"" + anchorId + "\"></a>");
                indexAnchors.add(new IndexAnchor(anchorId, index, verse));
                indexes.remove(index);
            }
            sb2.append(word.getContent() + " ");
        }
        for (Index index : indexes) {
            String anchorId = "i" + index.getId() + "v" + verse.getId();
            sb.append("<a id=\"" + anchorId + "\"></a>");
            indexAnchors.add(new IndexAnchor(anchorId, index, verse));
        }
        sb.append(verse.getWords().isEmpty() ? verseContent : sb2.toString());
    }
}
