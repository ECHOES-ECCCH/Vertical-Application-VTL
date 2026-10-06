package pl.psnc.vtl.portal.services.exports.tei;

import lombok.experimental.UtilityClass;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import pl.psnc.vtl.portal.dto.blocks.BlockDto;
import pl.psnc.vtl.portal.model.blocks.BlockType;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.exports.utils.BlockDtoUtil;
import pl.psnc.vtl.portal.services.exports.utils.XmlPrintingUtil;
import pl.psnc.vtl.portal.utils.BoundingBoxUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class TeiDocumentComposer {

    private static final Map<BlockType, String> zoneTypeMappings;
    static {
        Map<BlockType, String> map = new ConcurrentHashMap<>();
        map.put(BlockType.PARAGRAPH, "p");
        map.put(BlockType.HEADER, "head");
        map.put(BlockType.FOOTER, "footer");
        map.put(BlockType.HEADING_1, "divHead1");
        map.put(BlockType.HEADING_2, "divHead2");
        map.put(BlockType.HEADING_3, "divHead3");
        map.put(BlockType.IMAGE, "figure");
        map.put(BlockType.CAPTION, "caption");
        map.put(BlockType.PAGE_NO, "pageNum");
        map.put(BlockType.LIST, "list");
        map.put(BlockType.LIST_ITEM, "item");
        map.put(BlockType.TABLE, "table");
        map.put(BlockType.TABLE_ROW, "row");
        map.put(BlockType.TABLE_CELL, "cell");
        map.put(BlockType.ANNOTATION, "note");
        zoneTypeMappings = Collections.unmodifiableMap(map);
    }

    public Element composeNextPageSection(Page page, List<BlockDto> blocks) {
        //add new <tei:surface> element for page
        Element teiSurface = DocumentHelper.createElement("surface");
        teiSurface.addAttribute("id", "page_"+page.getPageNo());
        TeiElementComposerUtil.addBoundingBoxToElement(teiSurface, page.getBoundingBox());

        for (BlockDto block : blocks) {
            teiSurface.add(composeZone(block));
        }

        return teiSurface;
    }

    public String composeForPage(Page page, List<BlockDto> blocks) {
        Document document = DocumentHelper.createDocument();
        Element teiRoot = document.addElement("TEI");
        teiRoot.addElement("teiHeader");

        //create teiSourceDoc and add section for one page
        Element teiSourceDoc = teiRoot.addElement("sourceDoc");
        teiSourceDoc.add(composeNextPageSection(page, blocks));

        //build metadata in document
        TeiMetadataComposer.addMetadataFromPage(document, page);

        return XmlPrintingUtil.prettyPrintTei(document);
    }

    private Element composeZone(BlockDto block) {
        Element zone = DocumentHelper.createElement("zone");

        zone = TeiElementComposerUtil.addBoundingBoxToElement(zone,
                BoundingBoxUtils.getContainingBox(BlockDtoUtil.allVerses(block).stream().map(Verse::getBoundingBox)));
        String type = zoneTypeMappings.get(block.getType());
        if (type != null)
            zone.addAttribute("type", type);

        for (Object child : block.getChildren()) {
            if (child instanceof BlockDto) {
                zone.add(composeZone((BlockDto) child));
            } else if (BlockType.IMAGE == block.getType()) {
                zone.add(composeGraphicZone((Verse) child));
            } else {
                TeiElementComposerUtil.createLinesForContentVerses(zone, List.of((Verse) child));
            }
        }

        return zone;
    }

    private Element composeGraphicZone(Verse verse) {
        Element zone = DocumentHelper.createElement("zone");
        TeiElementComposerUtil.addBoundingBoxToElement(zone, verse.getBoundingBox());
        zone.addAttribute("type", "image");
        Element graphic = DocumentHelper.createElement("graphic");
        graphic.addAttribute("url", verse.getContent());
        zone.add(graphic);
        return zone;
    }
}
